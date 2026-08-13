# ─────────────────────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

ARG SERVICE_NAME

# Copy Maven wrapper from the selected service
COPY ${SERVICE_NAME}/mvnw ./mvnw
COPY ${SERVICE_NAME}/.mvn ./.mvn

# Copy the custom parent BOM
COPY uni-admit-bom ./uni-admit-bom

# Copy service POM
COPY ${SERVICE_NAME}/pom.xml ./${SERVICE_NAME}/pom.xml

# Copy service source
COPY ${SERVICE_NAME}/src ./${SERVICE_NAME}/src

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw -f ${SERVICE_NAME}/pom.xml dependency:go-offline -B 2>/dev/null || true

# Build service
RUN ./mvnw -f ${SERVICE_NAME}/pom.xml package -DskipTests -B


# ─────────────────────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Non-root user
RUN addgroup -S appgrp && adduser -S appuser -G appgrp

USER appuser

WORKDIR /app

ARG SERVICE_NAME

# Copy generated JAR
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]