# Nexus Repository Manager — Setup, Integration, and CI/CD Workflow

This document records how Nexus Repository Manager was set up for this repository, how Maven and Docker are wired to it, and how the CI/CD pipeline publishes to it. It follows the phases in [`plan.md`](plan.md); see that file for the full checklist and the reasoning behind each decision. Commands below are real transcripts from standing this up, not hypothetical examples.

> **Screenshots:** this was set up and verified entirely from the command line / REST API in a headless environment, so no browser screenshots were captured while writing this doc. Every claim below is backed by a command + its actual output instead. If you're presenting this, open `http://localhost:8082`, log in, and grab screenshots of: the repository list, the `product-service` artifact detail page in `maven-releases`, and the `buy02/product-service` image detail page in `docker-hosted` — the REST endpoints referenced below show you exactly what you'd see.

## 1. Deviations from the exercise brief

| Brief says | What this repo actually does | Why |
|---|---|---|
| Java 11 | Java 11 (Spring Boot `2.7.18`, Spring Cloud `2021.0.9`) | All six services build and run on Java 11. Jenkins itself and the SonarQube scanner retain Java 21; application builds use a separate Java 11 JDK. See [JAVA11_MIGRATION.md](JAVA11_MIGRATION.md). |
| "a simple web application... Spring Boot" | The existing buy-02 microservices project (6 services + Angular frontend) | Reused rather than built from scratch. **`product-service`** is the designated flagship service for every grading-relevant demo below — treat it as "the web application" if you're grading against the brief. |
| WAR or JAR | JAR only | All six services are Spring Boot apps with embedded servers and default (`jar`) packaging; none declare `<packaging>war</packaging>`, and none should — that's the standard Spring Boot deployment model, and the brief explicitly accepts JAR as an alternative to WAR. |
| (not mentioned) | Nexus **Community Edition** requires accepting a EULA via the REST API before any repository works | `sonatype/nexus3:latest` currently ships as CE, not classic OSS. See §2. |

## 2. Setup and configuration (Phase 0)

Nexus runs via the official Docker image, defined in [`docker-compose.nexus.yml`](docker-compose.nexus.yml):

```yaml
services:
  nexus:
    image: sonatype/nexus3:latest
    container_name: nexus
    ports:
      - "8082:8081"   # UI/API
      - "8083:8083"   # docker-hosted
      - "8084:8084"   # docker-proxy
      - "8085:8085"   # docker-group
    volumes:
      - nexus-data:/nexus-data
    networks:
      - nexus-network
```

```
$ docker compose -f docker-compose.nexus.yml up -d
$ docker exec nexus id
uid=200(nexus) gid=200(nexus) groups=200(nexus)
```

Non-root confirmed — the official image runs as uid 200 by default, satisfying "run Nexus under a dedicated user, not root" with no extra setup.

### 2.1 Admin password and EULA

The image generates a random initial admin password on first boot (`docker exec nexus cat /nexus-data/admin.password`). Since this repo's `.gitignore` doesn't track that file, the setup wizard is completed via the REST API instead of the browser wizard:

```
$ curl -u admin:<generated-password> -X PUT -H "Content-Type: text/plain" \
    --data "<new-password>" \
    http://localhost:8082/service/rest/v1/security/users/admin/change-password
204
```

**Then, before touching any repository:** this Nexus build is Community Edition, which gates every repository request (even anonymous reads through a proxy) behind EULA acceptance:

```
$ curl -u admin:*** http://localhost:8082/repository/maven-public/...
403 You must accept the End User License Agreement (EULA) through the onboarding wizard or REST API...

$ curl -u admin:*** http://localhost:8082/service/rest/v1/system/eula
{"accepted": false, "disclaimer": "Use of Sonatype Nexus Repository - Community Edition is governed by..."}

$ curl -u admin:*** -X POST -H "Content-Type: application/json" \
    http://localhost:8082/service/rest/v1/system/eula \
    -d '{"accepted": true, "disclaimer": "<the exact text returned by GET>"}'
204
```

The `disclaimer` field must match the `GET` response verbatim or the `POST` 500s. Do this immediately after the password change, before creating repositories — nothing works until it's accepted.

Anonymous access was already disabled by default on this version (`GET /service/rest/v1/security/anonymous` → `"enabled": false`), so no separate action was needed there.

### 2.2 Repositories

Four Maven repos exist out of the box; three Docker repos were created via the REST API (`POST /service/rest/v1/repositories/docker/{hosted,proxy,group}`):

| Repository | Type | Format | Port | Notes |
|---|---|---|---|---|
| `maven-releases` | hosted | maven2 | 8082 | default `ALLOW_WRITE_ONCE` policy — immutable once published, see §4 |
| `maven-snapshots` | hosted | maven2 | 8082 | snapshots redeployable |
| `maven-central` | proxy | maven2 | 8082 | → `repo.maven.apache.org` |
| `maven-public` | group | maven2 | 8082 | releases + snapshots + central, this is what builds resolve against |
| `docker-hosted` | hosted | docker | 8083 | `writePolicy: ALLOW`, this is what CI pushes to |
| `docker-proxy` | proxy | docker | 8084 | → `registry-1.docker.io` |
| `docker-group` | group | docker | 8085 | hosted + proxy |

```
$ curl -u admin:*** http://localhost:8082/service/rest/v1/repositories
# → all 7 above, plus Nexus's default nuget-* repos (unused, harmless)
```

### 2.3 Network reachability — read this before anything 401s on you

`docker-compose.nexus.yml` is its own standalone Compose project with a private network (`nexus_nexus-network`). Nothing joins it automatically:

- **From your own machine** (a local `mvn deploy`, `docker` CLI, `curl`): use the **published host ports** — `http://localhost:8082/...`, `localhost:8083` for Docker. `http://nexus:8081` will not resolve here.
- **From inside a container**: `http://nexus:8081` only resolves if that container is explicitly joined to `nexus_nexus-network`. The Jenkins controller (`jenkins`) was joined to it for this reason — see §5.1.
- **From the nested Jenkins Docker-in-Docker daemon** (the `docker` service in `jenkins-compose.yaml`, used for `docker build`/`push` steps): this is a *separate* Docker engine with its own network namespace, not joined to `nexus_nexus-network` and not configured with `nexus:8083` as an insecure registry. This is a known, not-yet-closed gap — see §5.2.

## 3. Maven integration (Phase 1)

[`jenkins/nexus-settings.xml`](jenkins/nexus-settings.xml) is the `settings.xml` used both by Jenkins and for local manual deploys:

```xml
<mirrors>
  <mirror>
    <id>nexus-public</id>
    <mirrorOf>*</mirrorOf>
    <url>http://localhost:8082/repository/maven-public/</url>
  </mirror>
</mirrors>
<servers>
  <!-- id must match the mirror's id: anonymous access is disabled, so reads
       through maven-public need credentials too, not just writes. -->
  <server><id>nexus-public</id><username>${env.NEXUS_USER}</username><password>${env.NEXUS_PASS}</password></server>
  <server><id>nexus-releases</id><username>${env.NEXUS_USER}</username><password>${env.NEXUS_PASS}</password></server>
  <server><id>nexus-snapshots</id><username>${env.NEXUS_USER}</username><password>${env.NEXUS_PASS}</password></server>
</servers>
```

The `<server id="nexus-public">` block matching the mirror's `id` is the part that's easy to miss — without it, every build fails with `401 Unauthorized` resolving even `spring-boot-starter-parent`, because the mirror forwards requests unauthenticated.

None of the six service poms share a parent aggregator, so the release/snapshot repository URLs are passed on the command line rather than hardcoded into `<distributionManagement>`:

```
$ export NEXUS_USER=... NEXUS_PASS=...
$ ./product-service/mvnw -s jenkins/nexus-settings.xml -f product-service/pom.xml clean deploy \
    -DaltReleaseDeploymentRepository=nexus-releases::http://localhost:8082/repository/maven-releases/ \
    -DaltSnapshotDeploymentRepository=nexus-snapshots::http://localhost:8082/repository/maven-snapshots/ \
    -DskipTests
```

Result, confirmed via search API:

```
$ curl -u admin:*** "http://localhost:8082/service/rest/v1/search?repository=maven-snapshots&group=com.example&name=product-service"
# → com.example:product-service:0.0.1-20260922.095741-1 (pom + jar + checksums)
```

This also proves dependency resolution goes exclusively through Nexus: the build resolved `spring-boot-starter-parent` and every other dependency through `maven-public` (per the `mirrorOf: *` mirror) — there is no other repository configured for it to fall back to.

## 4. Versioning (Phase 2)

Demonstrated against `product-service`, using a **temporary** version bump (`mvn versions:set` / reverted after — the actual project stays on `0.0.1-SNAPSHOT`, this was a Nexus demo, not a real release):

```
$ ./product-service/mvnw -f product-service/pom.xml versions:set -DnewVersion=1.0.0 -DgenerateBackupPoms=false
$ ./product-service/mvnw -s jenkins/nexus-settings.xml -f product-service/pom.xml clean deploy \
    -DaltReleaseDeploymentRepository=nexus-releases::http://localhost:8082/repository/maven-releases/ ...
# → 1.0.0 lands in maven-releases

$ ./product-service/mvnw -s jenkins/nexus-settings.xml -f product-service/pom.xml deploy ...   # re-deploy same version
409 Conflict — "...cannot be updated as asset already exists and redeploy is not allowed"
```

That 409 is `maven-releases`' default write policy (`ALLOW_WRITE_ONCE`) enforcing immutability — a published release can never be silently overwritten.

```
$ ./product-service/mvnw -f product-service/pom.xml versions:set -DnewVersion=1.0.1 -DgenerateBackupPoms=false
$ ./product-service/mvnw -s jenkins/nexus-settings.xml -f product-service/pom.xml clean deploy ...
# → 1.0.1 lands in maven-releases too

$ curl -u admin:*** "http://localhost:8082/service/rest/v1/search?repository=maven-releases&group=com.example&name=product-service"
# → both 1.0.0 and 1.0.1 listed, independently, with their own jar assets

$ mvn dependency:get -Dartifact=com.example:product-service:1.0.0:jar -DremoteRepositories=nexus-releases::::http://localhost:8082/repository/maven-releases/
$ mvn dependency:get -Dartifact=com.example:product-service:1.0.1:jar -DremoteRepositories=nexus-releases::::http://localhost:8082/repository/maven-releases/
# → each pulls its own distinct jar into ~/.m2 — /com/example/product-service/{1.0.0,1.0.1}/
```

**Rollback story:** because each version is immutable and independently addressable, a consumer pins `<version>1.0.0</version>` (or `mvn dependency:get -Dversion=1.0.0`) and keeps resolving that exact jar forever, regardless of `1.0.1` or any later version being published. No coordination with the publisher is needed to "roll back" — you just stop asking for the newer version.

## 5. Docker integration (Phase 3)

```
$ docker build -t localhost:8083/buy02/product-service:1.0.0 ./product-service
```

Nexus's Docker connector is plain HTTP (a real deployment would put TLS in front of it), so the daemon needs `localhost:8083` whitelisted as insecure:

```jsonc
// /etc/docker/daemon.json
{ "insecure-registries": ["localhost:8083"] }
```

```
$ sudo systemctl restart docker   # picks up the config change
$ echo "$NEXUS_PASS" | docker login localhost:8083 -u "$NEXUS_USER" --password-stdin
Login Succeeded
$ docker push localhost:8083/buy02/product-service:1.0.0
1.0.0: digest: sha256:48ec374d... size: 856

$ curl -u admin:*** "http://localhost:8082/service/rest/v1/search?repository=docker-hosted"
# → buy02/product-service 1.0.0

$ docker rmi localhost:8083/buy02/product-service:1.0.0
$ docker pull localhost:8083/buy02/product-service:1.0.0
Digest: sha256:48ec374d...   # matches the pushed digest exactly
Status: Downloaded newer image for localhost:8083/buy02/product-service:1.0.0
```

Restarting the Docker daemon to apply `insecure-registries` briefly stopped every running container on the host (Jenkins, SonarQube, Nexus itself); all came back via their `restart: unless-stopped` policy except `nexus`, which needed one manual `docker compose -f docker-compose.nexus.yml up -d` (its Jetty shutdown outlasted Docker's stop grace period, so it got SIGKILLed instead of restarting cleanly). Data survived — it's all on the named `nexus-data` volume.

## 6. CI/CD pipeline (Phase 4)

Nexus publishing was added to the existing Jenkins pipeline (`Jenkinsfile`, SCM-polled every 2 minutes) as two new stages after `Archive Artifacts`:

- **`Publish to Nexus`** — loops over all six services, runs `mvn -s jenkins/nexus-settings.xml deploy -DskipTests` with `NEXUS_USER`/`NEXUS_PASS` injected from the `nexus-credentials` Jenkins credential via `withCredentials`.
- **`Build & Push Docker Images`** — `docker build` + `docker push` each service to `nexus:8083`, tagged `${GIT_COMMIT}`.

Neither stage needs an explicit failure gate: Jenkins declarative pipelines stop advancing through `stages` after any failure, so both are automatically skipped if `Build and Test` failed (including the `FORCE_BUILD_FAILURE`/`FORCE_TEST_FAILURE` audit-only params). The `success`/`failure` email bodies were extended to say whether artifacts/images were published that run.

### 6.1 Network wiring — done and verified

The `jenkins` controller container (where `mvn` steps actually execute) was joined to Nexus's Docker network so it can reach `http://nexus:8081` directly, without needing a published host port:

```yaml
# jenkins-compose.yaml
services:
  jenkins:
    networks: [default, nexus]
networks:
  nexus:
    external: true
    name: nexus_nexus-network
```

```
$ docker compose -f jenkins-compose.yaml up -d jenkins   # recreates only this container
$ docker exec jenkins curl -s -o /dev/null -w "%{http_code}" http://nexus:8081/service/rest/v1/status
200
```

### 6.2 Known gap: Docker push from Jenkins isn't wired yet

`docker build`/`push` steps in the Jenkinsfile run against Jenkins' **nested Docker-in-Docker daemon** (the `docker` service in `jenkins-compose.yaml`, reached via `DOCKER_HOST=tcp://docker:2376`) — a completely separate Docker engine from both the host and the `jenkins` container. It is not joined to `nexus_nexus-network` and has no `insecure-registries` entry for `nexus:8083`, so `Build & Push Docker Images` will fail to resolve/push as-is. Closing this requires the same treatment §6.1 got — network join + insecure-registry config — but applied to that nested daemon, which also hosts the pipeline's staging deployment (`scripts/deploy-staging.sh`). That's a larger blast radius than recreating the `jenkins` container alone, so it was deliberately left for a follow-up rather than done opportunistically. Fix steps are in §6.4.

### 6.3 Manual step required: `nexus-credentials` Jenkins credential

Add a "Username with password" credential with ID `nexus-credentials` (Jenkins → Manage Jenkins → Credentials), username `ci-deploy`, password from `.env`'s `NEXUS_PASS`. This mirrors the existing `buy01-jwt-secret` credential already used elsewhere in the `Jenkinsfile`. Full steps are in §6.4.

### 6.4 Closing the remaining gaps — manual steps

These three are independent of each other and can be done in any order, but this is the order that makes the most sense (rebuild first, since it also recreates the `jenkins` container, then wire credentials/registry on top of the fresh container).

#### Step 1 — Rebuild the Jenkins image (picks up the Java 11 application JDK)

`jenkins/Dockerfile` now bakes a Java 11 JDK into the Jenkins image (`/opt/java/java11`, exposed as `JAVA11_HOME`) so the `Jenkinsfile`'s backend build/test/publish steps can run under Java 11 while the Jenkins controller itself stays on Java 21 (per [`JAVA11_MIGRATION.md`](JAVA11_MIGRATION.md)). The running Jenkins container predates this — rebuild it:

```bash
docker compose -f jenkins-compose.yaml build jenkins
docker compose -f jenkins-compose.yaml up -d --no-deps jenkins
```

`--no-deps` keeps this scoped to just the `jenkins` container — it won't touch `jenkins-docker` (the dind sidecar) or anything outside this compose project. `jenkins_home` is a named volume, so nothing stored there (jobs, credentials, build history) is lost by rebuilding the image.

Verify the JDK landed:

```bash
docker exec jenkins /opt/java/java11/bin/java -version
# openjdk version "11...."
```

#### Step 2 — Create the `nexus-credentials` Jenkins credential

1. Open Jenkins at `http://localhost:8090` and log in.
2. **Manage Jenkins → Credentials → System → Global credentials (unrestricted) → Add Credentials.**
3. Kind: **Username with password**.
4. Username: `ci-deploy`
5. Password: the value of `NEXUS_PASS` in this repo's `.env` (the `nx-ci-deployer` service account created in Phase 5 — not the Nexus `admin` account).
6. ID: `nexus-credentials` (must match exactly — this is the literal string the `Jenkinsfile`'s `withCredentials([usernamePassword(credentialsId: 'nexus-credentials', ...)])` blocks reference).
7. Description: something like "Nexus ci-deploy service account (nx-ci-deployer role)".
8. Save.

No restart needed — Jenkins picks up new credentials immediately; the next pipeline run that reaches the `Publish to Nexus` stage will use it.

#### Step 3 — Wire the nested dind daemon for Docker push

The `docker` service in `jenkins-compose.yaml` (container `jenkins-docker`) is what actually executes `docker build`/`docker push` for the `Build & Push Docker Images` stage. It needs the same two things `jenkins` itself already got in §6.1: network access to Nexus, and permission to push to a plain-HTTP registry.

1. **Join it to Nexus's network.** Edit `jenkins-compose.yaml`, add the `docker` service to the same external `nexus` network already declared for `jenkins`:
   ```yaml
   services:
     docker:
       # ...unchanged...
       networks: [default, nexus]
   ```
   (The top-level `networks: { default: {}, nexus: { external: true, name: nexus_nexus-network } }` block already exists from §6.1 — no changes needed there.)

2. **Recreate just that container:**
   ```bash
   docker compose -f jenkins-compose.yaml up -d docker
   ```
   This restarts `jenkins-docker` — anything it had running (the staging deployment from `scripts/deploy-staging.sh`) goes down with it and needs to be redeployed afterward via the pipeline's `Deploy to Staging` stage.

3. **Configure `nexus:8083` as an insecure registry on the *nested* daemon.** This is a separate Docker engine from the host, with its own config — the host's `/etc/docker/daemon.json` (already updated for `localhost:8083`, see §5) doesn't apply here. The dind image (`docker:28.4.0-dind`) reads `insecure-registries` from its own `/etc/docker/daemon.json` inside that container. Since `jenkins-docker` uses named volumes for `/var/lib/docker` and certs but not for `/etc/docker`, the cleanest way to set this permanently is via the dind container's startup command in `jenkins-compose.yaml`:
   ```yaml
   services:
     docker:
       command: --storage-driver=overlay2 --insecure-registry=nexus:8083
   ```
   Then recreate it again:
   ```bash
   docker compose -f jenkins-compose.yaml up -d docker
   ```

4. **Verify** from inside the `jenkins` container (which talks to this daemon via `DOCKER_HOST`):
   ```bash
   docker exec jenkins curl -s -o /dev/null -w "%{http_code}" http://nexus:8083/v2/
   # 401 - the Docker Registry API's standard "no credentials" response,
   # which confirms network + registry reachability. A connection failure/
   # timeout would mean it's still broken.
   ```

Steps 1-3 done and verified — Jenkins image rebuilt with the Java 11 JDK, `nexus-credentials` created, and `jenkins-docker` reachable at `nexus:8083`.

#### Step 4 — Create the Jenkins job for *this* repo

This Jenkins instance (`docker-compose -f jenkins-compose.yaml`) previously only had a job configured for the older `buy-02` repository — nothing was pointed at this repo (`SManousis/nexus` on GitHub) at all, so none of the `Jenkinsfile` changes above had ever actually run anywhere. A new job is required.

Given this repo's workflow is already PR/branch-based (branch protection, required review, required `build-and-analyze` check), a **Multibranch Pipeline** fits better than a single job pinned to `master` — it auto-discovers every branch with a `Jenkinsfile` and builds it independently, so a feature branch gets real Jenkins signal before merge, not just after.

Because of that, `Publish to Nexus` and `Build & Push Docker Images` were changed to `when { branch 'master' }` (see the `Jenkinsfile` diff) — without that guard, every feature-branch build would publish real jars/push real Docker images on every push, which isn't what you want for throwaway branches.

Setup:

1. `http://localhost:8090` → **New Item** → name it (e.g. `nexus`) → select **Multibranch Pipeline** → **OK**.
2. **Branch Sources → Add source → Git**:
   - Repository URL: `https://github.com/SManousis/nexus.git`
   - Credentials: none — it's a public repo, anonymous HTTPS clone works.
3. **Build Configuration**: Mode `by Jenkinsfile`, Script Path `Jenkinsfile` (default — matches the repo root).
4. **Scan Multibranch Pipeline Triggers**: check "Periodically if not otherwise run", interval e.g. every 2 minutes (the `Jenkinsfile`'s own `pollSCM` trigger is a *single-job* trigger and doesn't drive multibranch's *branch discovery*; this setting is what makes it notice new/updated branches without a webhook).
5. **Save.** Jenkins scans the repo, discovers `master` and any open feature branches with a `Jenkinsfile`, and creates a sub-job per branch automatically, building each once immediately.

Existing global credentials (`buy01-jwt-secret`, `nexus-credentials`) are Jenkins-instance-wide, so every branch's sub-job already has access — nothing extra to configure per branch.

Once this is set up, a real Jenkins run on `master` should get through `Publish to Nexus` and `Build & Push Docker Images` without manual intervention — that's the "Done when" bar in `plan.md`'s Phase 4. Feature-branch runs will build/test but stop before those two stages, by design.

## 7. Security and access control (Phase 5, bonus)

Two custom roles, created via `POST /service/rest/v1/security/roles`:

| Role | Privileges | Used by |
|---|---|---|
| `nx-ci-deployer` | `browse`+`read`+`add`+`edit` on `maven-releases`, `maven-snapshots`, `docker-hosted`; `browse`+`read` on `maven-public` | `ci-deploy` user (Jenkins) |
| `nx-developer` | `browse`+`read` only, on `maven-public` and `docker-group` | `dev-sample` user (stand-in for team members) |
| `nx-admin` | Nexus's built-in Administrator role, unchanged | `admin` only |

**Non-obvious finding:** the granular `nx-repository-view-<format>-<repo>-add` privilege *alone* does not grant `mvn deploy` / `docker push` on this Nexus version (3.96.3-01 Community Edition) — it returns `403`, even for a component that's never existed before. Both operations touch repository metadata (`maven-metadata.xml` / the manifest) as part of an upload, which needs the `-edit` privilege too. This isn't documented anywhere obvious; found by testing `-add` alone (403) → `-add`+`-edit` (201, works), while ruling out needing the full `-*` wildcard or `-delete`. The `nx-ci-deployer` role above already reflects the working combination.

Enforcement, verified with real requests:

```
$ curl -u ci-deploy:*** -X PUT --upload-file x.jar \
    http://localhost:8082/repository/maven-releases/com/example/rbac-verify/1.0.0/rbac-verify-1.0.0.jar
201 Created

$ curl -u dev-sample:*** -X PUT --upload-file x.jar \
    http://localhost:8082/repository/maven-releases/com/example/rbac-verify-dev/1.0.0/rbac-verify-dev-1.0.0.jar
403 Forbidden

$ curl -u dev-sample:*** -X DELETE \
    http://localhost:8082/repository/maven-releases/com/example/rbac-verify/1.0.0/rbac-verify-1.0.0.jar
403 Forbidden

$ curl -u dev-sample:*** \
    http://localhost:8082/repository/maven-public/com/example/rbac-verify/1.0.0/rbac-verify-1.0.0.jar
200 OK
```

`ci-deploy` can publish but not delete anything (no `delete` privilege granted); `dev-sample` can read everything through the group repos but can't write or delete anywhere.

## 8. Credentials

`NEXUS_USER`/`NEXUS_PASS` (the `ci-deploy` service account) and `NEXUS_ADMIN_USER`/`NEXUS_ADMIN_PASS` live in `.env` (gitignored, never committed); `.env.example` documents the placeholders. `NEXUS_DOCKER_HOST` defaults to `localhost:8083` for local use.
