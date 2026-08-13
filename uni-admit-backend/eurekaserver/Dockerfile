# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom first (better layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy BOM pom (needed for multi-module build)
# Download dependencies first (cached layer)
RUN ./mvnw dependency:go-offline -B 2>/dev/null || true

# Copy source
COPY src ./src

# Build the jar
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Non-root user for security
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port (override per service in docker-compose)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]