# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace/legalconnect

# Copy Maven wrapper and pom.xml first for better caching
COPY legalconnect/mvnw legalconnect/mvnw.cmd ./
COPY legalconnect/.mvn ./.mvn
COPY legalconnect/pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY legalconnect/src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests -B

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# Copy the fat jar from the build stage
COPY --from=builder /workspace/legalconnect/target/legalconnect-0.0.1-SNAPSHOT.jar /app/app.jar

# Change ownership to spring user
RUN chown spring:spring /app/app.jar

# Switch to non-root user
USER spring

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"
ENV SERVER_PORT=8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/v1/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"] 