# --- Build stage ---
FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /workspace

# Cache dependencies separately from source for faster rebuilds
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/build/libs/*.jar app.jar

# Cloud Run injects PORT and expects the container to listen on it
# (server.port=${PORT:8080} in application.yml already honors this)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
