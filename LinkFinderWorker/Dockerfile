# Step 1: Build Java application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY LinkFinderWorker/build/libs/*.jar app.jar

# Step 2: Install Go and GoLinkFinder
FROM golang:1.23 AS golinkfinder

# Set the working directory
WORKDIR /tools

# Install git and other necessary tools
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# Clone GoLinkFinder
RUN go install github.com/0xsha/GoLinkFinder@latest
# Step 3: Combine Java app and Python tool
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the Java application JAR from the builder stage
COPY --from=builder /app/app.jar /app/app.jar

# Copy Python libraries and binaries from the intermediate Python stage
COPY --from=golinkfinder /go/bin/GoLinkFinder /usr/local/bin/GoLinkFinder

# Copy configuration files
COPY config /config
# Copy the wrapper script into the container

# Set environment variables
ENV SHARED_CONFIG_LOCATION=/config/shared-container.yml
ENV PATH="/usr/local/bin:$PATH"
ENV LD_LIBRARY_PATH="/usr/local/lib:$LD_LIBRARY_PATH"

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
