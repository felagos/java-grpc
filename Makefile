.PHONY: generate-protos force-dependencies server-build server-bootRun server-run server-stop server-rebuild server-logs help

# Detectar el sistema operativo
UNAME_S := $(shell uname -s 2>/dev/null)
ifeq ($(UNAME_S),Linux)
    DETECTED_OS := Linux
    GRADLEW := ./gradlew
endif
ifeq ($(UNAME_S),Darwin)
    DETECTED_OS := Darwin
    GRADLEW := ./gradlew
endif
ifeq ($(OS),Windows_NT)
    DETECTED_OS := Windows
    GRADLEW := .\gradlew.bat
endif
ifndef DETECTED_OS
    DETECTED_OS := Windows
    GRADLEW := .\gradlew.bat
endif

help:
	@echo "Available commands:"
	@echo "  make generate-protos    - Generate proto files from grpc-shared module"
	@echo "  make force-dependencies - Force refresh dependencies and build all modules"
	@echo "  make server-build       - Build gRPC server module only"
	@echo "  make server-bootRun     - Run gRPC Spring Boot server locally (port 6565)"
	@echo "  make server-run         - Start gRPC server with Docker Compose"
	@echo "  make server-stop        - Stop gRPC server (Docker Compose)"
	@echo "  make server-rebuild     - Rebuild and restart gRPC server image"
	@echo "  make server-logs        - View server logs"

generate-protos:
	@echo "Generating protos in grpc-shared (after refactoring to centralize protos)..."
	$(GRADLEW) :grpc-shared:generateProto
	@echo "Proto generation completed!"

force-dependencies:
	@echo "Forcing dependencies refresh and building all modules..."
	$(GRADLEW) clean build --refresh-dependencies
	@echo "Dependencies forced and build completed for all modules!"

server-build:
	@echo "Building gRPC Spring Boot server module..."
	$(GRADLEW) :grpc-server:clean :grpc-server:build
	@echo "Server build completed!"

server-bootRun:
	@echo "Starting gRPC Spring Boot server locally on port 6565..."
	@echo "Press Ctrl+C to stop the server"
	$(GRADLEW) :grpc-server:bootRun

server-run:
	@echo "Starting gRPC server with Docker Compose..."
	cd grpc-server && docker-compose up -d
	@echo "Server started. Use 'docker-compose logs -f' to view logs"

server-stop:
	@echo "Stopping gRPC server..."
	cd grpc-server && docker-compose down
	@echo "Server stopped"

server-rebuild:
	@echo "Rebuilding gRPC server image..."
	cd grpc-server && docker-compose up -d --build
	@echo "Server rebuilt and restarted"

server-logs:
	@echo "Viewing server logs..."
	cd grpc-server && docker-compose logs -f
