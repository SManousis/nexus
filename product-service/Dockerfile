# Stage 1 — compile and package
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY lombok.config .
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2 — lean runtime image
FROM eclipse-temurin:11-jre
RUN apt-get update \
    && apt-get install --no-install-recommends --yes wget \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
