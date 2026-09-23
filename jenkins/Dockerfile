FROM eclipse-temurin:11-jdk AS application-jdk

FROM jenkins/jenkins:2.568.3-lts-jdk21

COPY --from=application-jdk /opt/java/openjdk /opt/java/java11
ENV JAVA11_HOME=/opt/java/java11

USER root

RUN apt-get update \
    && apt-get install --no-install-recommends --yes ca-certificates curl \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg \
        -o /etc/apt/keyrings/docker.asc \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && . /etc/os-release \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian ${VERSION_CODENAME} stable" \
        > /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get install --no-install-recommends --yes docker-ce-cli docker-compose-plugin \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
