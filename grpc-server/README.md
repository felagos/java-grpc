# gRPC Server Application

Multi-service gRPC server implementing banking services with different streaming patterns (unary, server streaming, client streaming, bidirectional). Features request validation, Docker support, and production-ready NGINX load balancing.

## Architecture Overview

```
grpc-server/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/grpc/course/
│       │       ├── ServerApplication.java
│       │       ├── common/
│       │       │   └── GrpcServer.java
│       │       ├── repository/
│       │       │   └── AccountRepository.java
│       │       ├── services/
│       │       │   ├── BankService.java
│       │       │   ├── TransferService.java
│       │       │   └── GuessService.java
│       │       ├── handlers/
│       │       │   ├── TransferRequestHandler.java
│       │       │   └── GuessRequestHandler.java
│       │       └── interceptor/
│       │           └── ValidationInterceptor.java
│       ├── proto/
│       │   ├── account-balance.proto
│       │   ├── bank-service.proto
│       │   ├── transfer-service.proto
│       │   └── guess-number.proto
│       └── resources/
│           ├── application.properties
│           └── logback.xml
├── build.gradle
├── docker-compose.yml
├── Dockerfile
├── nginx.conf
└── settings.gradle
```

## Requirements

- Java 21 (configured via Gradle toolchain)
- Gradle 9.2.1 (included with wrapper)
- Protocol Buffers 3.25.5 (auto-managed)
- gRPC 1.74.0 (netty, protobuf, stub)
- Docker & Docker Compose (optional, for containerized deployment)

## Building

```bash
# Generate Protocol Buffer classes and build
.\gradlew.bat build

# Generate proto classes only
.\gradlew.bat generateProto

# Clean build artifacts
.\gradlew.bat clean
```

## Running the Server

### Local Development
```bash
# Start gRPC server on port 6565
.\gradlew.bat run
```

### Docker Deployment
```bash
# Using Docker Compose (recommended)
make server-run     # Start server + NGINX load balancer
make server-logs    # View container logs
make server-stop    # Stop all containers

# Manual Docker build
docker build -t grpc-server .
docker run -p 6565:6565 grpc-server
```

The server starts on **port 6565** with all services registered and validation enabled.

## gRPC Services

### BankService
- `GetAccountBalance` (Unary): Query account balance
- `GetAllAccounts` (Server Streaming): Stream all account data
- `Withdraw` (Server Streaming): Withdraw money, receive denominations as stream
- `Deposit` (Client Streaming): Stream deposit amounts, get final balance

### TransferService  
- `TransferMoney` (Bidirectional Streaming): Real-time money transfers between accounts
  - Requires session affinity in load balancers
  - Uses dedicated `TransferRequestHandler` for complex logic

### GuessService
- `GuessNumber` (Bidirectional Streaming): Interactive number guessing game
  - Demonstrates bidirectional streaming patterns
  - Uses `GuessRequestHandler` for game state management

## Features

### Request Validation
- **buf/protovalidate**: Automatic request validation using proto constraints
- **ValidationInterceptor**: Server-side validation for all requests
- **Example constraints**: `account_number > 0`, `amount > 0`

### Data Management
- **AccountRepository**: Thread-safe in-memory storage
- **Pre-initialized data**: 5 accounts (IDs 1-5) with balances $100-$500
- **Concurrent access**: Safe for multiple streaming connections

## Configuration

### Server Settings
```properties
# src/main/resources/application.properties
grpc.server.port=6565
```

### Load Balancing (Production)
**NGINX Configuration** (`nginx.conf`):
- **Session affinity required** for bidirectional streaming (`ip_hash`)
- **Disable buffering** (`grpc_buffering off`) for real-time streams
- **Health checks** at `/health` endpoint
- **Custom error handling** for gRPC status codes

### Server Features
- **Keep-alive settings**: 10s keepAliveTime, 1s timeout, 25s maxConnectionIdle
- **Graceful shutdown**: Runtime shutdown hooks
- **Automatic service registration**: All services auto-discovered

## Development Notes

### Protocol Buffers
- **Proto files**: `src/main/proto/*.proto`
- **Generated classes**: `build/generated/source/proto/main/java/` (DO NOT EDIT)
- **Package mapping**: Proto messages → `com.grpc.course` package
- **Regeneration**: Run `.\gradlew.bat generateProto` after modifying `.proto` files

### Streaming Implementation
- **Unary**: Simple request-response
- **Server Streaming**: Use `StreamObserver.onNext()` multiple times + `onCompleted()`
- **Client Streaming**: Return `StreamObserver<Request>`, accumulate in `onNext()`
- **Bidirectional**: Requires dedicated handler classes for complex logic

### Critical Patterns
- **Always call** `responseObserver.onCompleted()` to signal stream end
- **Thread safety**: Handler classes must handle concurrent streams
- **Error handling**: Use `onError()` instead of throwing exceptions
- **Validation**: All requests automatically validated via interceptor
