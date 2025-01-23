# Use the latest Java 21 JDK slim image as the base
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the build output JAR from the monorepo to the container
# Assuming the build.gradle builds the JAR into build/libs/
COPY certificateTransparencyWorker/build/libs/*.jar app.jar

# Copy the shared config directory from the monorepo into the container
# Adjust the relative path if needed
COPY config /config

# Set environment variable to reference the shared config location
ENV SHARED_CONFIG_LOCATION=/config/shared-container.yml

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
