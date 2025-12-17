# Builder stage
# Using Gradle 8.x with JDK 21
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Final stage
# Using Eclipse Temurin JDK 21 (LTS) on Alpine
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Volumes for persistent data
VOLUME /home/media
VOLUME /keys

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]