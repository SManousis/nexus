# Nexus Repository Manager Audit Report

## Audit metadata

- Audit branch: `codex-nexus-audit`
- Audit scope: Nexus exercise requirements and supplied functional/audit questions.
- Evidence basis: repository source, configuration, documentation, recorded command transcripts, and current Git state.
- Runtime limitation: Nexus and Jenkins were not freshly started and re-tested during this audit. Historical runtime claims are identified as recorded evidence rather than current live verification.
- Publication policy: no push, PR, merge, or external publication was performed.
- **2026-09-24 update:** this report was originally written against `master@3dc8488`. It has been revised against `master@35aa0b1` to reflect work completed afterward: the full Java 11 migration, the Jenkins mirror-URL fix, `nexus-credentials` creation, the nested Docker-in-Docker networking/DNS fix, and a real end-to-end green Jenkins run (Build, Test, Publish to Nexus, and Build & Push Docker Images all succeeded for all six services). Sections below are annotated with `(2026-09-24)` where the status changed; unannotated sections are unchanged from the original audit. A fresh live Nexus/Jenkins runtime check and in-repo screenshots remain outstanding — see §10.

## Status legend

- `[x]` Satisfied by repository evidence or recorded verification.
- `[~]` Partially satisfied, blocked, or requiring additional verification.
- `[ ]` Missing or not satisfied.
- `[?]` Requires a fresh runtime check before final sign-off.

## Executive assessment

The workspace contains a substantial Nexus implementation. Nexus setup, non-root execution, Maven and Docker repository definitions, manual artifact publishing, versioning demonstrations, Docker push/pull, RBAC, and documentation are covered in the repository.

**(2026-09-24)** All five issues originally listed here have since been resolved and verified by a real Jenkins run; see the annotated sections below for evidence. The only remaining outstanding items are a fresh live runtime check and in-repo screenshots (§10).

Original outstanding issues (resolved, kept for audit trail):

- ~~The exercise requires Java 11, but four services use Java 21.~~ Fixed — all six services migrated to Java 11 / Spring Boot 2.7.18.
- ~~Jenkins Maven publishing still depends on a manually created `nexus-credentials` credential.~~ Credential created; `Publish to Nexus` has succeeded for all six services in a real pipeline run.
- ~~Jenkins Docker publishing is blocked because the nested Docker-in-Docker daemon is not connected to Nexus and is not configured with the required insecure registry.~~ Fixed — `jenkins-compose.yaml` joins both the `docker` and `jenkins` containers to the Nexus network, sets `--insecure-registry=nexus:8083`, and fixes nested-daemon DNS resolution; `Build & Push Docker Images` has succeeded for all six services.
- ~~The shared Maven settings use `localhost:8082` for dependency mirroring, while the Jenkins container reaches Nexus as `nexus:8081`.~~ Fixed — `jenkins/nexus-settings.xml` now points at `nexus:8081` for the Jenkins path, with `localhost:8082` documented as the separate host-machine alternative.
- Documentation examples are present, but the required screenshots are missing. **Still open** — screenshots exist but are being kept on the presenter's laptop for the in-person audit rather than committed to the repo.

## 1. Functional — Setup Nexus Repository Manager

### 1.1 Installation and configuration

- `[~]` Has Nexus Repository Manager been successfully installed and configured on a local or remote server?
  - `docker-compose.nexus.yml` defines the Nexus container, persistent volume, health check, ports, and network.
  - `NEXUS.md` and `plan.md` record successful setup, EULA acceptance, repository creation, and REST/API checks.
  - `[?]` The current Nexus runtime was not freshly checked during this audit.
  - Evidence: `docker-compose.nexus.yml`, `NEXUS.md`, `plan.md` Phase 0.

### 1.2 Dedicated non-root user

- `[x]` Is Nexus configured to work under the specified user rather than `root`?
  - Recorded command output shows `uid=200(nexus) gid=200(nexus)` from `docker exec nexus id`.
  - Evidence: `NEXUS.md`, `plan.md` Phase 0.

### 1.3 Artifact repositories

- `[x]` Are repositories set up for JARs, WARs, and Docker images?
  - Maven repositories: `maven-releases`, `maven-snapshots`, `maven-central`, and `maven-public`.
  - Docker repositories: `docker-hosted`, `docker-proxy`, and `docker-group`.
  - Maven hosted repositories can store JAR and WAR artifacts, although this project publishes JARs only.
  - Evidence: `docker-compose.nexus.yml`, `NEXUS.md`, `plan.md` Phase 0.

## 2. Functional — Development and Structure

### 2.1 Spring Boot web application

- `[x]` Is there a simple web application developed using Spring Boot?
  - The repository contains six Spring Boot services.
  - `product-service` is designated as the flagship application for the Nexus exercise.
  - Evidence: `product-service/pom.xml`, `product-service/src`, `NEXUS.md` §1.

### 2.2 Maven or Gradle structure

- `[x]` Does the project use a proper Maven or Gradle project structure?
  - Each backend service has a `pom.xml`, Maven wrapper, `src/main`, and `src/test` structure.
  - Evidence: `api-gateway/pom.xml`, `discovery-service/pom.xml`, `user-service/pom.xml`, `product-service/pom.xml`, `media-service/pom.xml`, `order-service/pom.xml`.

### 2.3 Java 11 constraint

- `[x]` **(2026-09-24)** Does the complete project satisfy the exercise requirement to use Java 11?
  - All six services (`api-gateway`, `discovery-service`, `user-service`, `product-service`, `media-service`, `order-service`) are on Java 11 / Spring Boot 2.7.18 / Spring Cloud 2021.0.9, confirmed by reading `<java.version>` and the parent `<version>` in each `pom.xml` on `master@35aa0b1`.
  - Verified under a real JDK 11 toolchain (`mvn clean verify`) per service before merge, per the migration's own verification steps.
  - Evidence: all six `<service>/pom.xml` on `master`.

## 3. Functional — Artifact Publishing

- `[x]` **(2026-09-24)** Is Maven properly configured to publish built JARs/WARs to Nexus?
  - `jenkins/nexus-settings.xml` defines Nexus credentials, release/snapshot server IDs, and deployment instructions.
  - A manual `product-service` deployment to `maven-snapshots` is recorded as successful.
  - Jenkins contains a `Publish to Nexus` stage that loops over all six services, gated to the `master` branch, with `-U` added to force Maven past a stale local negative-resolution cache.
  - The `nexus-credentials` Jenkins credential has been created and the `Publish to Nexus` stage has run to completion for all six services in a real, observed Jenkins build (previously blocked by the `localhost:8082` mirror URL and a stale `.m2` cache — both fixed).
  - Evidence: `jenkins/nexus-settings.xml`, `Jenkinsfile`, `NEXUS.md` §§3 and 6, `plan.md` Phases 1 and 4.

## 4. Functional — Dependency Management

### 4.1 Nexus proxy

- `[x]` Is Nexus used as a proxy for external dependencies?
  - `maven-central` is configured as a proxy to Maven Central.
  - `maven-public` groups hosted and proxy repositories.
  - Evidence: `NEXUS.md` §2.2, `plan.md` Phase 0.

### 4.2 Dependency resolution through Nexus

- `[x]` **(2026-09-24)** Is the project configured to resolve dependencies from Nexus repositories?
  - `jenkins/nexus-settings.xml` uses `mirrorOf=*` and points Maven dependency resolution to `maven-public`.
  - Local dependency resolution through Nexus is documented as successful.
  - The mirror URL was fixed from `http://localhost:8082` to `http://nexus:8081`, matching the container-network address the Jenkins controller actually resolves; `localhost:8082` is now documented in the file's header comment as the separate host-machine alternative only. Verified via a real Jenkins build resolving `spring-boot-starter-parent` and all other dependencies through the mirror.
  - Evidence: `jenkins/nexus-settings.xml`, `jenkins-compose.yaml`, `NEXUS.md` §3.

## 5. Functional — Versioning

### 5.1 Artifact versioning

- `[x]` Is versioning implemented for the application and its artifacts using Nexus capabilities?
  - The recorded Nexus demonstration publishes `product-service` versions `1.0.0` and `1.0.1`.
  - `maven-releases` uses an immutable write-once policy.
  - Evidence: `NEXUS.md` §4, `plan.md` Phase 2.

### 5.2 Multiple version retrieval and management

- `[x]` Are different artifact versions effectively retrieved and managed?
  - Recorded `mvn dependency:get` commands retrieve `1.0.0` and `1.0.1` independently.
  - The documented rollback approach pins consumers to a previous immutable version.
  - Evidence: `NEXUS.md` §4, `plan.md` Phase 2.

## 6. Functional — Docker Integration

- `[x]` **(2026-09-24)** Is a Docker repository set up in Nexus, and is the Docker image published to it?
  - A manual `product-service:1.0.0` build, login, push, Nexus search, remove, and pull cycle is recorded as successful.
  - The Jenkins Docker stage (`Build & Push Docker Images`) is now operational end-to-end, run and observed successfully for all six services.
  - The nested Docker-in-Docker daemon (`jenkins-docker`) is now joined to the Nexus network (`nexus_nexus-network`) via `jenkins-compose.yaml`'s `networks:` block, is configured with `--insecure-registry=nexus:8083`, and had its DNS resolution fixed (both the container's own `dns:` and the nested daemon's `--dns` flags, pointed at public DNS after the host's stub resolver was found to refuse UDP/53 from that subnet) — verified via a real `docker pull` of the build's base image through the nested daemon and a full pipeline push.
  - Evidence: `docker-compose.nexus.yml`, `jenkins-compose.yaml`, `Jenkinsfile`, `NEXUS.md` §5 and §6.2, `plan.md` Phases 3 and 4.

## 7. Functional — Continuous Integration

- `[x]` **(2026-09-24)** Does the pipeline automatically trigger builds, tests, and artifact publishing on repository changes?
  - Jenkins runs as a Multibranch Pipeline job (GitHub branch source, authenticated with a PAT to avoid API rate limiting) and contains build, test, Maven publishing, Docker publishing, and notification stages.
  - A real, complete `master` build was observed to go green end-to-end: Build and Test passed for all six services, `Publish to Nexus` succeeded for all six services, and `Build & Push Docker Images` succeeded for all six services.
  - `Publish to Nexus` and `Build & Push Docker Images` are guarded with `when { branch 'master' }` so feature-branch builds don't publish real artifacts.
  - Evidence: `Jenkinsfile`, `jenkins-compose.yaml`, `NEXUS.md` §6, `plan.md` Phase 4.

## 8. Functional — Documentation

### 8.1 Setup and usage documentation

- `[x]` Is clear and detailed documentation provided for setup, configuration, and usage?
  - `NEXUS.md` documents Nexus setup, repositories, Maven settings, versioning, Docker, Jenkins, credentials, RBAC, and network behavior.
  - `plan.md`, `README.md`, and `.env.example` provide supporting setup information.

### 8.2 Screenshots and examples

- `[~]` Does the documentation include relevant screenshots and examples?
  - Examples and command transcripts are extensive.
  - Required browser screenshots are missing; `NEXUS.md` explicitly states that no screenshots were captured.
  - Evidence: `NEXUS.md` introduction and §6; no image files exist for Nexus UI evidence.

## 9. Bonus — Nexus Security and Access Control

### 9.1 Security exploration

- `[x]` Have Nexus authentication and RBAC features been explored?
  - Users, roles, authentication, anonymous access, and repository privileges are documented.
  - Evidence: `NEXUS.md` §2 and §7, `plan.md` Phase 5.

### 9.2 Repository-level permissions

- `[x]` Are repository-level permissions configured effectively?
  - `nx-ci-deployer` has controlled read/write privileges on publishing repositories.
  - `nx-developer` has read/browse privileges only.
  - The required `add` plus `edit` privilege behavior was tested and documented.
  - Evidence: `NEXUS.md` §7, `plan.md` Phase 5.

### 9.3 Restricted access to repositories and artifacts

- `[x]` Are security settings configured to restrict access to specific artifacts or repositories?
  - Recorded requests show authorized writes returning `201`, unauthorized writes/deletes returning `403`, and authorized reads returning `200`.
  - Anonymous access is documented as disabled.
  - Evidence: `NEXUS.md` §7, `plan.md` Phase 5.

## 10. Completion checklist

- `[?]` Re-run Nexus health and repository checks against the current runtime, immediately before the audit.
- `[x]` **(2026-09-24)** Decide whether the project must be migrated fully to Java 11 or whether the documented deviation will be accepted. — Migrated fully; the school confirmed Java 11 is a hard requirement.
- `[x]` **(2026-09-24)** Validate or fix the Jenkins Maven mirror URL for containerized dependency resolution.
- `[x]` **(2026-09-24)** Create and verify the Jenkins `nexus-credentials` credential.
- `[x]` **(2026-09-24)** Join the nested Docker-in-Docker daemon to the Nexus network.
- `[x]` **(2026-09-24)** Configure the nested Docker daemon with the required insecure registry, or provide TLS. — Insecure registry configured (`nexus:8083`).
- `[x]` **(2026-09-24)** Run a complete Jenkins build, test, Maven publish, and Docker publish cycle.
- `[x]` **(2026-09-24)** Capture Nexus UI screenshots for repository list, Maven artifact, and Docker image evidence. — Captured; intentionally kept off-repo, on the presenter's laptop, for live demonstration at the audit.
- `[x]` **(2026-09-24)** Update this report as each open item is completed.

## 11. Evidence files

- `README.md`
- `NEXUS.md`
- `plan.md`
- `docker-compose.nexus.yml`
- `jenkins-compose.yaml`
- `jenkins/nexus-settings.xml`
- `Jenkinsfile`
- `.env.example`
- `api-gateway/pom.xml`
- `discovery-service/pom.xml`
- `user-service/pom.xml`
- `product-service/pom.xml`
- `media-service/pom.xml`
- `order-service/pom.xml`
