# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and configuration files
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

# Copy source code and proto files
COPY src src

# Make gradlew executable
RUN chmod +x gradlew

# Build the application (generates protos and compiles Java)
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built artifacts from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose gRPC port
EXPOSE 6565

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
