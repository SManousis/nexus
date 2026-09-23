# Java 11 migration

All six backend applications target Java 11, Spring Boot 2.7.18, and Spring
Cloud 2021.0.9. This supersedes the original Java 21 exception in the Nexus
exercise plan. The frontend is unchanged.

## Compatibility changes

- Records became immutable Lombok value classes, retaining their existing
  constructors and fluent accessors. Explicit Jackson creators and property
  names preserve HTTP and Kafka payloads, including legacy `imageUrls` aliases.
- Configuration classes use constructor binding and field validation. MongoDB
  URLs now use Boot 2.7's `spring.data.mongodb.uri`; the `MONGODB_URI`
  environment variable remains unchanged.
- Servlet and validation imports use `javax`. MVC test slices and mock beans
  use the Spring Boot 2.7 APIs. Security matchers retain their route order.
- HTTP clients use `RestTemplate`, with two-second connect and three-second
  read timeouts. User-service keeps its discovery-backed, load-balanced client.
  Order-service uses Apache HttpClient so stock updates still support PATCH.
- Kafka producers adapt Spring Kafka 2.8's future API. Serializers, topic
  names, partition keys, and message schemas remain unchanged.
- Java text blocks, switch expressions, pattern matching, and newer stream
  APIs were replaced with Java 11 equivalents.
- `maven.compiler.release=11` prevents accidental use of newer JDK APIs.
  Deploy plugin 3.1.4 is explicitly pinned to preserve the existing
  `repository-id::url` Nexus deployment arguments.
- Media-service's headless JVM option now retains JaCoCo's agent. Lombok marks
  generated methods with `@Generated`, so coverage reports measure handwritten
  code; no SonarQube quality conditions or source exclusions were relaxed.

## Local verification

Use a real Java 11 JDK for builds and tests:

```bash
export JAVA11_HOME=/path/to/jdk-11
for service in api-gateway discovery-service user-service product-service media-service order-service; do
  JAVA_HOME="$JAVA11_HOME" ./product-service/mvnw -B -ntp -f "$service/pom.xml" clean verify
done
```

The existing `scripts/verify.sh` also accepts `JAVA11_HOME` and checks the
application JDK before running backend, frontend, and Compose verification.

## Validation results

On 2026-09-23, all 212 backend tests passed under a Temurin Java 11 JDK:

| Service | Tests |
| --- | ---: |
| API gateway | 22 |
| Discovery | 1 |
| User | 36 |
| Product | 55 |
| Media | 33 |
| Order | 65 |

All application class files were checked for Java 11 bytecode (major version
55). All six application Dockerfiles and the Jenkins Dockerfile built
successfully. The Jenkins review image executes both its Java 21 runtime and
the Java 11 application JDK. The effective POM resolves deploy plugin 3.1.4.

An isolated eight-container stack (six services, MongoDB, Kafka) passed an
end-to-end smoke test through the gateway: registration/login, JWT access
control, request validation, image upload/download, avatar ownership through
discovery, product creation with media ownership, cart persistence, checkout
and stock PATCH, order retrieval, cancellation with stock restoration, and
Kafka-driven image cleanup after product deletion. Test containers and their
separate data volumes were removed afterward; existing deployments were not
modified. No images or artifacts were published to Nexus during validation.

## CI and runtime rollout

GitHub Actions installs Java 11 for `clean verify`, then runs SonarScanner on
Java 21 against those compiled classes. `sonar.java.jdkHome` points to the
Java 11 JDK. The quality gate still blocks the workflow on failure.

For local Sonar scans, set `JAVA11_HOME` to Java 11, `JAVA_HOME` to Java 21,
and the usual `SONAR_HOST_URL` / `SONAR_TOKEN`, then run
`scripts/run-sonar-backend.sh`. It targets the same `nexus-*` projects as CI.

Jenkins itself stays on Java 21. Its Dockerfile adds a separate Java 11 JDK
at `/opt/java/java11`, exposed as `JAVA11_HOME`. The Jenkinsfile uses that JDK
for backend builds/tests and Maven publishing. **Rebuild the Jenkins image
before running the migrated Jenkinsfile** after this branch is reviewed:

```bash
docker compose -f jenkins-compose.yaml build jenkins
docker compose -f jenkins-compose.yaml up -d --no-deps jenkins
```

Application Dockerfiles build with Maven 3.9 / Java 11 and run on a Java 11
JRE. They include `wget` for the existing Compose health checks. Rebuild
application images as part of the normal deployment; old Java 21 images do
not change merely because the POM changed.

No database migration is required by the value-class conversion. Regression
tests cover order/cart Mongo mapping, JSON round trips, configuration binding
and validation, HTTP authorization forwarding, real PATCH transport, and
legacy event aliases. SonarQube's final new-code gate remains a CI check.

## References

- [Spring Boot 2.7.18 reference](https://docs.spring.io/spring-boot/docs/2.7.18/reference/htmlsingle/)
- [SonarScanner runtime requirements](https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/scanners/scanner-environment/general-requirements)
- [SonarQube Java analysis JDK configuration](https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/languages/java)
