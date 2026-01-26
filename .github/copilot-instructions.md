# Copilot Instructions for java-grpc Project

## Project Overview
Multi-module Java gRPC application showcasing different streaming patterns (unary, server streaming, client streaming, bidirectional). Built with pure gRPC (not Spring Boot), using Gradle for builds and Protocol Buffers for service definitions.

## Architecture & Key Components

### Module Structure
- **grpc-server/**: gRPC server implementing banking services with validation
- **grpc-client/**: Multiple client applications demonstrating different gRPC patterns
- **Shared proto files**: Service definitions synchronized between modules

### Generated Code Structure
- **Proto files**: `src/main/proto/*.proto` - Define message schemas and gRPC services
- **Generated Java**: `build/generated/source/proto/main/java/` - Auto-generated from proto files (NEVER EDIT)
- **Package mapping**: Proto messages generate into `com.grpc.course` package

### Core Dependencies
- **gRPC**: v1.74.0 (netty-shaded, protobuf, stub)
- **Protocol Buffers**: v3.25.5
- **Java**: Version 21 (via toolchain)
- **Validation**: buf/protovalidate for request validation
- **Logging**: SLF4J + Logback

## Critical Developer Workflows

### Building Both Modules
```bash
# Root level - builds both modules
.\gradlew.bat build

# Or build specific modules
cd grpc-server && .\gradlew.bat build
cd grpc-client && .\gradlew.bat build

# Generate protos only
.\gradlew.bat generateProto
```

### Running Server & Clients
```bash
# Start server (port 6565)
cd grpc-server && .\gradlew.bat run

# Run different client types
cd grpc-client && .\gradlew.bat run  # UnaryClient (default)
cd grpc-client && .\gradlew.bat run --args="ServerStreaming"
cd grpc-client && .\gradlew.bat run --args="ClientStreaming"
```

### Docker Support
```bash
# Server with Docker Compose
make server-run     # Start server in Docker
make server-logs    # View logs
make server-stop    # Stop container
```

## Project-Specific Conventions

### Package Structure
- **Services**: `com.grpc.course.services` - gRPC service implementations
- **Handlers**: `com.grpc.course.handlers` - StreamObserver implementations for streaming
- **Repository**: `com.grpc.course.repository` - In-memory data storage (AccountRepository)
- **Common**: `com.grpc.course.common` - Shared utilities (GrpcServer, GrpcClient)
- **Interceptors**: `com.grpc.course.interceptor` - Request validation logic
- **Generated**: `com.grpc.course` - All proto-generated classes

### Service Implementation Pattern
All gRPC services extend generated `*ImplBase` classes:
```java
public class BankService extends BankServiceGrpc.BankServiceImplBase {
    @Override
    public void getAccountBalance(BalanceCheckRequest request, 
                                 StreamObserver<AccountBalance> responseObserver) {
        // Implementation with validation, logging, and error handling
    }
}
```

### Streaming Patterns
- **Server Streaming**: Return `Iterator<Response>` or use `StreamObserver.onNext()` multiple times
- **Client Streaming**: Return `StreamObserver<Request>`, accumulate in `onNext()`, emit in `onCompleted()`
- **Bidirectional**: Both sides stream simultaneously using dedicated handler classes

### Validation Integration
Proto messages use buf/validate constraints:
```proto
message WithdrawRequest {
    int32 account_number = 1 [(buf.validate.field).int32 = {gt: 0}];
    int32 amount = 2 [(buf.validate.field).int32 = {gt: 0}];
}
```
Server automatically validates all requests via `ValidationInterceptor`.

## Critical Implementation Details

### Server Configuration
- **Port**: 6565 (hardcoded in `ServerApplication.GRPC_PORT`)
- **Services**: BankService, TransferService, GuessService auto-registered
- **Validation**: All services wrapped with `ValidationInterceptor`
- **Keep-alive**: 10s keepAliveTime, 1s timeout, 25s maxConnectionIdle

### Account Data
- **Pre-initialized**: 5 accounts (IDs 1-5) with balances $100-$500
- **Thread-safe**: AccountRepository uses thread-safe Map operations
- **In-memory**: Data resets on server restart

### Client Connection Management
- **Two channels**: `channel` (standard) and `channelAlive` (with keep-alive)
- **Configuration**: `application.properties` sets host/port (`grpc.server.port=6565`)

## Streaming Architecture Details

### Handler Pattern for Complex Streams
- **TransferRequestHandler**: Implements bidirectional streaming for money transfers
- **GuessRequestHandler**: Implements bidirectional streaming for number guessing game
- **Pattern**: Separate handler classes implement `StreamObserver<Request>` for complex logic

### Client-Side Streaming Implementation
- **Deposit pattern**: Send account number first, then multiple money amounts
- **Transfer pattern**: Send multiple transfer requests, collect responses
- **CountDownLatch**: Used for coordinating async operations in client streaming

## Load Balancing & Production Considerations

### NGINX Configuration
- **Session affinity required** for bidirectional streaming (`ip_hash` in upstream)
- **Disable buffering** (`grpc_buffering off`) for real-time streaming
- **Health checks** at `/health` endpoint
- **Error handling**: Custom gRPC error pages (502 -> grpc-status 14)

## Common Pitfalls

### Proto Code Generation
- Always run `.\gradlew.bat generateProto` after modifying `.proto` files
- Generated classes appear in `build/generated/source/proto/main/java/`
- Import generated messages directly: `import com.grpc.course.AccountBalance;`
- **Never** edit generated classes manually

### StreamObserver Lifecycle
- **Always** call `responseObserver.onCompleted()` to signal stream end
- For streaming: call `onNext()` multiple times, then `onCompleted()`
- Handle errors with `onError()` instead of throwing exceptions

### Bidirectional Streaming Caveats
- **Requires session affinity** in load balancers (same client → same server instance)
- **Thread safety**: Handler classes must be thread-safe for concurrent streams
- **Resource cleanup**: Always implement `onError()` and `onCompleted()` properly

### Build Dependencies
- Proto generation runs automatically during `build` task
- Use `.\gradlew.bat` (not `gradle`) for consistent Gradle 9.2.1
- Client requires server running on localhost:6565

## Key Files to Reference
- [grpc-server/src/main/java/com/grpc/course/ServerApplication.java](grpc-server/src/main/java/com/grpc/course/ServerApplication.java) - Server startup and service registration
- [grpc-server/src/main/java/com/grpc/course/common/GrpcServer.java](grpc-server/src/main/java/com/grpc/course/common/GrpcServer.java) - Server configuration and lifecycle
- [grpc-client/src/main/java/com/grpc/course/common/GrpcClient.java](grpc-client/src/main/java/com/grpc/course/common/GrpcClient.java) - Client connection management
- [grpc-server/src/main/java/com/grpc/course/interceptor/ValidationInterceptor.java](grpc-server/src/main/java/com/grpc/course/interceptor/ValidationInterceptor.java) - Request validation with buf/protovalidate
- [grpc-server/src/main/proto/*.proto](grpc-server/src/main/proto/) - Service definitions and message schemas
- [grpc-server/nginx.conf](grpc-server/nginx.conf) - Load balancing configuration for production
- [build.gradle](grpc-server/build.gradle) - Protobuf plugin configuration and dependencies
