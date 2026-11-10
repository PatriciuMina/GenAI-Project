# syntax=docker/dockerfile:1
# Multi-stage build for the Spring Boot backend.
# Stage 1 builds the jar with the Maven wrapper; stage 2 runs it on a small JRE image.
# Tests are NOT run here (they need Docker for Testcontainers); CI runs `./mvnw verify`.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
# Wrapper + pom first, so the dependency download is cached until pom.xml changes.
# --strict-checksums: fail the build if a downloaded jar does not match Maven Central's checksum
# (Maven only warns by default, and a corrupted jar then breaks the app at runtime).
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q --strict-checksums dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q --strict-checksums package -DskipTests && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
