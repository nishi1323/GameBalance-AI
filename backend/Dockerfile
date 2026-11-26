# Stage 1: Build the Spring Boot application with Maven and JDK 17
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Download dependencies first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source and build the executable fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight Java 17 JRE runtime
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Ensure up-to-date CA certificates in the container
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create and use a non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

COPY --from=builder /build/target/gamebalance-backend-1.0.0.jar /app/app.jar
RUN chown appuser:appgroup /app/app.jar

USER appuser

# Render sets PORT dynamically at runtime; defaults to 8080 locally.
# Use TLSv1.2 by default to prevent JDK 17 TLS 1.3 handshake internal_error alerts with Atlas proxies,
# while preserving full certificate and hostname verification (overridable via JAVA_OPTS).
ENV PORT=8080
ENV JAVA_OPTS="-Djdk.tls.client.protocols=TLSv1.2"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 ${JAVA_OPTS} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
