# ======================================================================
# Multi-stage Dockerfile for Hridaya Creations backend
# ======================================================================

# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies first
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy AS runtime

# Create a non-root user
RUN groupadd --system hridaya && useradd --system --gid hridaya hridaya

WORKDIR /app
COPY --from=build /workspace/target/hridaya-creations-backend.jar app.jar

# Writable logs directory
RUN mkdir -p /app/logs && chown -R hridaya:hridaya /app
USER hridaya

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Honor an injected PORT (PaaS) and fall back to 8080 locally.
HEALTHCHECK --interval=30s --timeout=5s --start-period=50s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT:-8080}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
