#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${SONAR_HOST_URL:-}" || -z "${SONAR_TOKEN:-}" ]]; then
  echo "Skipping backend Sonar upload because SONAR_HOST_URL and SONAR_TOKEN are not configured."
  exit 0
fi

: "${JAVA11_HOME:?Set JAVA11_HOME to the Java 11 application JDK}"
: "${JAVA_HOME:?Set JAVA_HOME to the Java 21 scanner JDK}"

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root/product-service"

services=(api-gateway discovery-service media-service order-service product-service user-service)
for service in "${services[@]}"; do
  echo "Building and testing ${service} with Java 11"
  JAVA_HOME="$JAVA11_HOME" ./mvnw -B -ntp -f "../${service}/pom.xml" clean verify

  echo "Analyzing ${service} with the scanner JDK"
  ./mvnw -B -ntp -f "../${service}/pom.xml" sonar:sonar \
    -Dsonar.projectKey="nexus-${service}" \
    -Dsonar.projectName="nexus-${service}" \
    -Dsonar.host.url="$SONAR_HOST_URL" \
    -Dsonar.token="$SONAR_TOKEN" \
    -Dsonar.java.jdkHome="$JAVA11_HOME" \
    -Dsonar.coverage.jacoco.xmlReportPaths="target/site/jacoco/jacoco.xml" \
    -Dsonar.qualitygate.wait=true
done
