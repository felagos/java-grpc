.PHONY: generate-protos force-dependencies certs-generate server-run server-stop server-rebuild server-logs help

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
	@echo "  make generate-protos   - Generate proto files in both grpc-client and grpc-server"
	@echo "  make force-dependencies - Force refresh dependencies and build both modules"
	@echo "  make certs-generate    - Generate SSL/TLS certificates for gRPC"
	@echo "  make server-run        - Start the gRPC server with Docker Compose"
	@echo "  make server-stop       - Stop the gRPC server (Docker Compose)"
	@echo "  make server-rebuild    - Rebuild and restart the gRPC server image"
	@echo "  make server-logs       - View server logs"

generate-protos:
	@echo "Generating protos in grpc-client..."
	cd grpc-client && $(GRADLEW) generateProto
	@echo "Generating protos in grpc-server..."
	cd grpc-server && $(GRADLEW) generateProto
	@echo "Proto generation completed!"

force-dependencies:
	@echo "Forcing dependencies refresh and building grpc-client..."
	cd grpc-client && $(GRADLEW) clean build --refresh-dependencies
	@echo "Forcing dependencies refresh and building grpc-server..."
	cd grpc-server && $(GRADLEW) clean build --refresh-dependencies
	@echo "Dependencies forced and build completed for both modules!"
certs-generate:
	@echo "Generating SSL/TLS certificates..."
ifeq ($(DETECTED_OS),Windows)
	cd grpc-server && generate-certs.bat
else
	cd grpc-server && chmod +x generate-certs.sh && ./generate-certs.sh
endif
	@echo "Certificates generated successfully!"


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
