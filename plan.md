# Nexus Plan — Artifact Management for Buy-02

This document plans the Nexus Repository Manager exercise on top of the existing buy-02 codebase already checked into this repository (Discovery, Gateway, User, Product, Media, Order services + Angular frontend, documented in [`README.md`](README.md), CI'd via `Jenkinsfile`). It follows the same convention as `buy02Plan.md`: phased checklists, each ending with what "done" looks like, so progress can be tracked and checked off rather than re-read from scratch.

Nothing here has been built yet — this is the plan to implement, not a record of completed work.

## Key decisions and callouts

- **Java version conflict.** The exercise brief says "Must use Java 11 and a compatible version of Maven." This repo's services target **Java 21** with Spring Boot `4.1.0` / Spring Cloud `2025.1.2`, neither of which supports Java 11 (Spring Boot 3.x+ requires 17 minimum). Downgrading the base buy-02 services to Java 11 is out of scope for an artifact-management exercise and would break the existing project. **Decision: keep Java 21/current Maven, document the deviation explicitly in `NEXUS.md`.** Flag this to whoever grades/reviews against the brief.
- **Nexus deployment method.** Run Nexus via the official `sonatype/nexus3` Docker image rather than a bare-metal install. That image already runs its process as the non-root `nexus` user (uid `200`) by default, which satisfies "run Nexus under a dedicated `nexus` user, not root" without any extra OS-user setup. It also matches this repo's existing pattern of infra-as-compose-file (`docker-compose.sonar.yml`, `jenkins-compose.yaml`).
- **Edition.** Nexus3 OSS (free), not Pro — Pro isn't needed for hosted/proxy/group Maven+Docker repos or basic RBAC.
- **Docker registry TLS.** Nexus's Docker repos need their own HTTP(S) connector port; for a local/lab setup we'll expose plain HTTP and register the host as an `insecure-registries` entry in the Docker daemon used for `docker push`/`pull` (documented explicitly as a lab-only shortcut — a real deployment would terminate TLS in front of Nexus).
- **Host ports.** `8081` is already taken by `jenkins-docker`'s nested staging port mapping (`jenkins-compose.yaml`), so Nexus UI/API will publish on host `8082` (container `8081`, Nexus's default internal port) to avoid collision. Docker repo connectors get `8083` (hosted), `8084` (proxy), `8085` (group).

## New files this plan introduces

| File | Purpose |
|---|---|
| `docker-compose.nexus.yml` | Nexus service definition, volume, network wiring into the existing compose stack (matches `docker-compose.sonar.yml` naming) |
| `jenkins/nexus-settings.xml` | Maven `settings.xml` template with `<mirror>` to the Nexus group repo and `<server>` credentials pulled from `${env.NEXUS_USER}` / `${env.NEXUS_PASS}` — no secrets committed |
| `NEXUS.md` | Setup, integration, and CI/CD workflow documentation with screenshots (per Instructions §8) |
| `.env.example` additions | `NEXUS_USER`, `NEXUS_PASS`, `NEXUS_DOCKER_HOST` placeholders |
| `Jenkinsfile` edits | New `Publish to Nexus` and `Build & Push Docker Images` stages |

## Phase 0 — Nexus setup and deployment (Instructions §1)

- [ ] Add `docker-compose.nexus.yml` with a `nexus` service (`sonatype/nexus3:latest`, named volume `nexus-data:/nexus-data`, host port `8082:8081`), joined to the same Docker network as the rest of the stack so Jenkins/services can reach it as `http://nexus:8081`.
- [ ] Start it standalone (`docker compose -f docker-compose.nexus.yml up -d`), retrieve the generated admin password (`docker exec ... cat /nexus-data/admin.password`), and complete the setup wizard (change admin password, disable anonymous access per the security phase below).
- [ ] Confirm the container process runs as `nexus`, not root (`docker exec nexus id`).
- [ ] Verify/adjust the repositories Nexus creates out of the box: `maven-releases` (hosted, release), `maven-snapshots` (hosted, snapshot), `maven-central` (proxy → `repo.maven.apache.org`), `maven-public` (group: releases + snapshots + central).
- [ ] Create the Docker repositories (not present by default): `docker-hosted` (hosted, HTTP connector `8083`), `docker-proxy` (proxy → `registry-1.docker.io`, HTTP connector `8084`), `docker-group` (group of the two, HTTP connector `8085`).

**Done when:** Nexus UI is reachable at `http://localhost:8082`, all five repositories listed above exist and are healthy, and `docker exec nexus id` shows a non-root user.

## Phase 1 — Maven integration (Instructions §2–3)

- [ ] Confirm the six existing Maven modules (`api-gateway`, `discovery-service`, `user-service`, `product-service`, `media-service`, `order-service`) already build deployable JARs via `./mvnw clean package` — no repo-side changes needed here, they already do.
- [ ] Add `jenkins/nexus-settings.xml`: a `<mirror mirrorOf="*">` pointing at `http://nexus:8081/repository/maven-public/` (so all dependency resolution goes exclusively through Nexus, per Instructions §4) and a `<server id="nexus-releases">` / `<server id="nexus-snapshots">` block with credentials from environment variables.
- [ ] Deploy artifacts using `-DaltReleaseDeploymentRepository=nexus-releases::http://nexus:8081/repository/maven-releases/` and `-DaltSnapshotDeploymentRepository=nexus-snapshots::http://nexus:8081/repository/maven-snapshots/` on the `mvn deploy` command line (via `maven-deploy-plugin` 3.x, already inherited from the Spring Boot parent) — this avoids hand-editing `<distributionManagement>` into all six poms individually since none of them share a parent aggregator pom.
- [ ] Run one manual `./mvnw -s jenkins/nexus-settings.xml -f product-service/pom.xml clean deploy` locally against the running Nexus to validate credentials and URLs before wiring it into CI.

**Done when:** a manual `mvn deploy` from a local machine, using `jenkins/nexus-settings.xml` and env-var credentials, lands a jar in `maven-snapshots` (or `maven-releases`) visible in the Nexus UI, and a clean `mvn dependency:resolve` pulls every dependency through the `maven-public` group (confirm via Nexus's request logs — no direct `repo.maven.apache.org` traffic).

## Phase 2 — Versioning (Instructions §5)

- [ ] Bump one service's `<version>` (currently `0.0.1-SNAPSHOT` across all six) to a release version (e.g. `1.0.0`) and deploy to `maven-releases`; confirm Nexus rejects a re-deploy of the same release version (default hosted-repo policy), demonstrating immutability.
- [ ] Deploy a second version (`1.0.1`) and show both are independently retrievable/browsable in the Nexus UI and via a `mvn dependency:get -Dversion=1.0.0` vs `1.0.1`.
- [ ] Document how this supports rollback: pin a consuming service's dependency (or the deployment tag) to `1.0.0` and show it resolves the older jar unaffected by `1.0.1` being published.

**Done when:** `NEXUS.md` shows two versions of the same artifact browsable side-by-side in Nexus with commands used to fetch each specific version.

## Phase 3 — Docker integration (Instructions §6)

- [ ] Build one service's image using its existing `Dockerfile` (e.g. `docker build -t localhost:8083/buy02/product-service:1.0.0 ./product-service`).
- [ ] Add `localhost:8083` (or the LAN host:port used) to the build/push machine's Docker daemon `insecure-registries` config.
- [ ] `docker login localhost:8083` with Nexus deployer credentials, `docker push`, confirm the image and its layers appear under `docker-hosted` in the Nexus UI.
- [ ] `docker pull` the same tag on a different path (e.g. after `docker rmi` locally) to confirm retrieval works end-to-end.
- [ ] Optionally point the frontend/dev workflow's base-image pulls (e.g. `node:*`, `eclipse-temurin:*` used in the Dockerfiles) at `docker-group` to demonstrate the proxy caching external base images too.

**Done when:** `docker pull localhost:8083/buy02/product-service:1.0.0` succeeds on a clean host after the image was only ever pushed to Nexus (not pulled from Docker Hub).

## Phase 4 — CI/CD pipeline integration (Instructions §7)

This repo's CI is Jenkins (`Jenkinsfile`, polling SCM every 2 minutes; a separate GitHub Actions self-hosted runner exists only for SonarQube per `instructions.md`). Nexus publishing belongs in the Jenkins pipeline, not a new workflow.

- [ ] Add a Jenkins `usernamePassword` credential `nexus-credentials` (mirrors the existing `buy01-jwt-secret` pattern already used in `Jenkinsfile`).
- [ ] Add a `Publish to Nexus` stage after the existing `Archive Artifacts` stage: loop over the six services, run `mvn -s jenkins/nexus-settings.xml deploy -DskipTests` (tests already ran in `Build and Test`) with `NEXUS_USER`/`NEXUS_PASS` injected from the credential.
- [ ] Add a `Build & Push Docker Images` stage: `docker build` + `docker push` each service's image to `docker-hosted`, tagged with `${GIT_COMMIT}` and/or the Maven version.
- [ ] Gate both new stages the same way `Deploy to Staging` is already gated (skip on `FORCE_BUILD_FAILURE`/`FORCE_TEST_FAILURE` audit runs; only run on successful build+test).
- [ ] Extend the existing `emailext` success/failure bodies to mention whether artifacts/images were published this run.

**Done when:** a push to the polled branch triggers Jenkins, which builds, tests, publishes jars to `maven-releases`/`maven-snapshots`, builds and pushes Docker images to `docker-hosted`, and reports this in the build's email notification — all without manual steps.

## Phase 5 — Security and access control (Instructions §9, bonus)

- [ ] Disable anonymous access in Nexus (enabled by default in a fresh install).
- [ ] Create roles: `nx-ci-deployer` (write to `maven-releases`, `maven-snapshots`, `docker-hosted`), `nx-developer` (read `maven-public`, `docker-group`), `nx-admin` (existing default admin role, restricted to actual admins).
- [ ] Create users: a `ci-deploy` service account (used only by Jenkins, credentials stored in the Jenkins credential store / `.env`, never committed — matches this repo's existing `.env`/`JWT_SECRET` handling) with `nx-ci-deployer`; individual named accounts for team members with `nx-developer`.
- [ ] Apply repository-level content selectors/privileges so `nx-developer` cannot write to hosted repos or delete artifacts.
- [ ] Verify with `curl -u ci-deploy:*** ... -X PUT` (should succeed) vs the same call with a `nx-developer` account (should 403).

**Done when:** `NEXUS.md` documents the role/user matrix and shows one successful and one rejected request demonstrating the enforcement.

## Phase 6 — Documentation (Instructions §8)

- [ ] Write `NEXUS.md` covering: Nexus install/config steps actually taken, repository list with types/policies/ports, `jenkins/nexus-settings.xml` usage, the Java-11-vs-21 deviation note, Docker push/pull walkthrough, CI stage additions, and the RBAC matrix from Phase 5.
- [ ] Include command transcripts and screenshots (repository list, a pushed artifact's detail page, a pushed Docker image's detail page, the Jenkins pipeline run with the new stages green).
- [ ] Cross-link `NEXUS.md` from the main `README.md` the way `PHASE3_WORKFLOW.md`/`PHASE5_WORKFLOW.md` are already linked/discoverable.

**Done when:** a reader unfamiliar with this repo can follow `NEXUS.md` alone to stand Nexus back up and reproduce a publish + pull cycle.

## Evaluation-criteria mapping

| Evaluation criterion | Answered by |
|---|---|
| 🏗️ Setup & Configuration | Phase 0 |
| ⚙️ Integration (Maven + Docker) | Phases 1, 3 |
| 🚀 Automation (CI/CD) | Phase 4 |
| 🔄 Version Control | Phase 2 |
| 🔐 Security | Phase 5 |
| 📘 Documentation | Phase 6 |

## Note on existing `plan.md`

Before this rewrite, `plan.md` contained a single line that looks like a leaked SonarQube user-token string (`sqa_...`), not a plan. It has been overwritten by this document. If that token is real, treat it as compromised and revoke/regenerate it in SonarQube — it was sitting in a tracked file.
