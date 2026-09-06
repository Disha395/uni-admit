FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy BOM first
COPY uni-admit-bom ./uni-admit-bom

# Copy service
COPY admission-service/mvnw ./mvnw
COPY admission-service/.mvn ./.mvn
COPY admission-service/pom.xml ./admission-service/pom.xml
COPY admission-service/src ./admission-service/src

RUN chmod +x ./mvnw
RUN ./mvnw -f admission-service/pom.xml dependency:go-offline -B 2>/dev/null || true
RUN ./mvnw -f admission-service/pom.xml package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
WORKDIR /app
COPY --from=builder /app/admission-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]