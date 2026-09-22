# Nexus Repository Manager Audit Report

## Audit metadata

- Audit branch: `codex-nexus-audit`
- Audit scope: Nexus exercise requirements and supplied functional/audit questions.
- Evidence basis: repository source, configuration, documentation, recorded command transcripts, and current Git state.
- Runtime limitation: Nexus and Jenkins were not freshly started and re-tested during this audit. Historical runtime claims are identified as recorded evidence rather than current live verification.
- Publication policy: no push, PR, merge, or external publication was performed.

## Status legend

- `[x]` Satisfied by repository evidence or recorded verification.
- `[~]` Partially satisfied, blocked, or requiring additional verification.
- `[ ]` Missing or not satisfied.
- `[?]` Requires a fresh runtime check before final sign-off.

## Executive assessment

The workspace contains a substantial Nexus implementation. Nexus setup, non-root execution, Maven and Docker repository definitions, manual artifact publishing, versioning demonstrations, Docker push/pull, RBAC, and documentation are covered in the repository.

The main outstanding issues are:

- The exercise requires Java 11, but four services use Java 21.
- Jenkins Maven publishing still depends on a manually created `nexus-credentials` credential.
- Jenkins Docker publishing is blocked because the nested Docker-in-Docker daemon is not connected to Nexus and is not configured with the required insecure registry.
- The shared Maven settings use `localhost:8082` for dependency mirroring, while the Jenkins container reaches Nexus as `nexus:8081`; this requires validation or a CI-specific settings configuration.
- Documentation examples are present, but the required screenshots are missing.

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

- `[ ]` Does the complete project satisfy the exercise requirement to use Java 11?
  - API Gateway and Discovery use Java 11/Spring Boot 2.7.18.
  - User, Product, Media, and Order services use Java 21/Spring Boot 4.1.0.
  - The deviation is documented in `NEXUS.md`, but documentation does not satisfy the stated constraint.

## 3. Functional — Artifact Publishing

- `[~]` Is Maven properly configured to publish built JARs/WARs to Nexus?
  - `jenkins/nexus-settings.xml` defines Nexus credentials, release/snapshot server IDs, and deployment instructions.
  - A manual `product-service` deployment to `maven-snapshots` is recorded as successful.
  - Jenkins contains a `Publish to Nexus` stage that loops over all six services.
  - `[~]` The Jenkins credential `nexus-credentials` is a required manual setup step and the complete Jenkins publishing path has not been run end-to-end.
  - Evidence: `jenkins/nexus-settings.xml`, `Jenkinsfile`, `NEXUS.md` §§3 and 6, `plan.md` Phases 1 and 4.

## 4. Functional — Dependency Management

### 4.1 Nexus proxy

- `[x]` Is Nexus used as a proxy for external dependencies?
  - `maven-central` is configured as a proxy to Maven Central.
  - `maven-public` groups hosted and proxy repositories.
  - Evidence: `NEXUS.md` §2.2, `plan.md` Phase 0.

### 4.2 Dependency resolution through Nexus

- `[~]` Is the project configured to resolve dependencies from Nexus repositories?
  - `jenkins/nexus-settings.xml` uses `mirrorOf=*` and points Maven dependency resolution to `maven-public`.
  - Local dependency resolution through Nexus is documented as successful.
  - Potential CI configuration issue: the mirror URL is `http://localhost:8082`, but the Jenkins container reaches Nexus through `http://nexus:8081`. The Jenkins-specific path needs validation or a separate settings file.
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

- `[~]` Is a Docker repository set up in Nexus, and is the Docker image published to it?
  - A manual `product-service:1.0.0` build, login, push, Nexus search, remove, and pull cycle is recorded as successful.
  - The Jenkins Docker stage is present but is not operational end-to-end.
  - The nested Docker-in-Docker daemon is not joined to the Nexus network and has no `nexus:8083` insecure-registry configuration.
  - Evidence: `docker-compose.nexus.yml`, `Jenkinsfile`, `NEXUS.md` §5 and §6.2, `plan.md` Phases 3 and 4.

## 7. Functional — Continuous Integration

- `[~]` Does the pipeline automatically trigger builds, tests, and artifact publishing on repository changes?
  - Jenkins polls SCM every two minutes and contains build, test, Maven publishing, Docker publishing, and notification stages.
  - Maven publishing is blocked until `nexus-credentials` is created in Jenkins.
  - Docker publishing is blocked by the nested Docker daemon networking/configuration gap.
  - Therefore, the pipeline structure exists, but the complete automated publishing workflow is not yet proven or fully operational.
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

- `[ ]` Re-run Nexus health and repository checks against the current runtime.
- `[ ]` Decide whether the project must be migrated fully to Java 11 or whether the documented deviation will be accepted.
- `[ ]` Validate or fix the Jenkins Maven mirror URL for containerized dependency resolution.
- `[ ]` Create and verify the Jenkins `nexus-credentials` credential.
- `[ ]` Join the nested Docker-in-Docker daemon to the Nexus network.
- `[ ]` Configure the nested Docker daemon with the required insecure registry, or provide TLS.
- `[ ]` Run a complete Jenkins build, test, Maven publish, and Docker publish cycle.
- `[ ]` Capture Nexus UI screenshots for repository list, Maven artifact, and Docker image evidence.
- `[ ]` Update this report as each open item is completed.

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
