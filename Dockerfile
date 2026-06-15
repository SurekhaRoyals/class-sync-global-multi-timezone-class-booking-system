
# ClassSync Global Class Booking System — Dockerfile
# Multi-stage build:
#   Stage 1 (builder) — compiles and packages the JAR
#   Stage 2 (runtime) — runs the JAR in a minimal JRE image

FROM eclipse-temurin:21-jdk-alpine AS builder


WORKDIR /app


# Copying pom.xml and mvnw BEFORE source code is a Docker
# layer caching optimisation.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw


# This layer is cached as long as pom.xml doesn't change
RUN ./mvnw dependency:go-offline -B


# Only copied AFTER dependencies — so source changes don't
# invalidate the dependency cache layer above
COPY src ./src

# Build the JAR 
# package → compiles + runs tests (skipped) + creates JAR
# RUN ./mvnw package -DskipTests -B
RUN ./mvnw package -DskipTests -B && ls -la target

# The JAR is created at:
# /app/target/global-class-booking-1.0.0.jar


# STAGE 2: RUNTIME 
# eclipse-temurin:17-jre-alpine → JRE only 
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root user
# Never run application as root inside a container.
# If the container is compromised, a non-root user limits damage.
# -r → system user (no login shell)
# -g → create group with same name
RUN addgroup -S classsync && adduser -S classsync -G classsync

# Set working directory
WORKDIR /app

# Copy only the JAR from builder stage
# Nothing else from builder is copied — no source, no Maven,
# no .class files, no test resources. Clean runtime image.
COPY --from=builder /app/target/*.jar app.jar

# Give ownership of the app directory to non-root user
RUN chown -R classsync:classsync /app

# Switch to non-root user for all subsequent commands
USER classsync

# JVM flags
# -Duser.timezone=UTC
#   Force JVM to UTC regardless of host machine's timezone.
#   Critical — without this, LocalDateTime.now() uses host timezone.
#   Our entire UTC storage convention depends on this being set.
#
# -XX:+UseContainerSupport
#   Tells JVM to respect container CPU/memory limits (not host limits).
#   Without this, JVM reads host RAM and allocates too much heap.
#
# -XX:MaxRAMPercentage=75.0
#   Allocate 75% of container memory to JVM heap.
#   e.g. container has 512 MB → JVM heap = ~384 MB
#   Leaves 25% for OS, off-heap, Metaspace, threads.
#
# -Djava.security.egd=file:/dev/./urandom
#   Speeds up SecureRandom initialisation in containers.
#   Default /dev/random can block on low-entropy systems.
ENV JAVA_OPTS="-Duser.timezone=UTC \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

# Expose application port 
EXPOSE 8080

# Health check
# Docker periodically runs this command to check if the container
# is healthy. If it fails 3 times, container is marked unhealthy.
HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=60s \
            --retries=3 \
            CMD wget -qO- http://localhost:8080/api/v1/parent/offerings || exit 1

# Start the application
# exec form (JSON array) is preferred over shell form.
# exec form: process runs directly (PID 1) — receives OS signals correctly.
# shell form: process runs under /bin/sh -c — signals may not propagate,
#             causing container to not shut down cleanly on SIGTERM.
ENTRYPOINT ["java","-jar","app.jar"]
