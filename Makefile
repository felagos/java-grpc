.PHONY: generate-protos server-run help

help:
	@echo "Available commands:"
	@echo "  make generate-protos   - Generate proto files in both grpc-client and grpc-server"
	@echo "  make server-run        - Start the gRPC server"

generate-protos:
	@echo "Generating protos in grpc-client..."
	cd grpc-client && .\gradlew.bat generateProto
	@echo "Generating protos in grpc-server..."
	cd grpc-server && .\gradlew.bat generateProto
	@echo "Proto generation completed!"

server-run:
	@echo "Starting gRPC server..."
	cd grpc-server && .\gradlew.bat bootRun
