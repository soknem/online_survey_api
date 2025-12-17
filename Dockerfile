# Builder stage
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Final stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# These instructions inform Docker that these paths are intended for mounting
VOLUME /home/media
VOLUME /keys

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]