.PHONY: generate-protos server-run server-stop help

# Detectar el sistema operativo
UNAME_S := $(shell uname -s 2>/dev/null)
ifeq ($(UNAME_S),Linux)
    DETECTED_OS := Linux
endif
ifeq ($(UNAME_S),Darwin)
    DETECTED_OS := Darwin
endif
ifeq ($(OS),Windows_NT)
    DETECTED_OS := Windows
endif
ifndef DETECTED_OS
    DETECTED_OS := Windows
endif

help:
	@echo "Available commands:"
	@echo "  make generate-protos   - Generate proto files in both grpc-client and grpc-server"
	@echo "  make server-run        - Start the gRPC server"
	@echo "  make server-stop       - Stop the gRPC server"

generate-protos:
	@echo "Generating protos in grpc-client..."
	cd grpc-client && .\gradlew.bat generateProto
	@echo "Generating protos in grpc-server..."
	cd grpc-server && .\gradlew.bat generateProto
	@echo "Proto generation completed!"

server-run:
	@echo "Starting gRPC server..."
	cd grpc-server && .\gradlew.bat run

server-stop:
	@echo "Stopping gRPC server..."
	ifeq ($(DETECTED_OS),Windows)
		@for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr :6565') do taskkill /F /PID %%a 2>nul
	else
		@lsof -i :6565 | grep -v COMMAND | awk '{print $$2}' | xargs kill -9 2>/dev/null || echo "No process found on port 6565"
	endif
		@echo "Server stopped"
