# Use the latest Java 21 JDK slim image as the base
FROM eclipse-temurin:21-jdk AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the build output JAR from the monorepo to the container
# Assuming the build.gradle builds the JAR into build/libs/
COPY getJsWorker/build/libs/*.jar app.jar

# Step 2: Install Go and JsFinder
FROM golang:1.23 AS jsfinder

# Set the working directory
WORKDIR /tools

# Install git and other necessary tools
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# Clone JsFinder
RUN git clone https://github.com/kacakb/jsfinder.git

# Build JsFinder
WORKDIR /tools/jsfinder
RUN go build -o /usr/local/bin/jsfinder

# Step 3: Combine Java app and JsFinder
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the Java application JAR from the builder stage
COPY --from=builder /app/app.jar /app/app.jar

# Copy JsFinder binary from the Go build stage
COPY --from=jsfinder /usr/local/bin/jsfinder /usr/local/bin/jsfinder

# Copy the shared config directory from the monorepo into the container
# Adjust the relative path if needed
COPY config /config

# Set environment variable to reference the shared config location
ENV SHARED_CONFIG_LOCATION=/config/shared-container.yml

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

