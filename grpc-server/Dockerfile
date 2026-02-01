# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy root level gradle files and settings
COPY settings.gradle .
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .

# Copy all module directories
COPY grpc-shared grpc-shared
COPY grpc-server grpc-server
COPY grpc-client grpc-client

# Fix line endings and make gradlew executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Build grpc-server (generates protos and compiles Java)
RUN ./gradlew -p grpc-server clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built artifacts from builder stage
COPY --from=builder /app/grpc-server/build/libs/*.jar app.jar

# Expose gRPC port
EXPOSE 6565

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
