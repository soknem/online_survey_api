# Builder stage
# Updated to Gradle 8.14 to support Spring Boot 4.0.0
FROM gradle:8.14-jdk21-alpine AS builder
WORKDIR /app
COPY . .

# Build the jar, skipping tests
RUN gradle build --no-daemon -x test

# Final stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Volumes for persistent data
VOLUME /home/media
VOLUME /keys

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]