
# ClassSync Global Class Booking System — Dockerfile

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw package -DskipTests -B && ls -la target

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S classsync && adduser -S classsync -G classsync

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN chown -R classsync:classsync /app

USER classsync

ENV JAVA_OPTS="-Duser.timezone=UTC \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=60s \
            --retries=3 \
            CMD wget -qO- http://localhost:8080/api/v1/parent/offerings || exit 1

ENTRYPOINT ["java","-jar","app.jar"]