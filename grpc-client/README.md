# gRPC Client Applications

Multiple client applications demonstrating different gRPC streaming patterns with the banking services. Connects to gRPC server to perform account operations, money transfers, and interactive games.

## Project Structure

```
grpc-client/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/grpc/course/
│       │       ├── ClientApplication.java           # Unary calls
│       │       ├── ServerStreamingApplication.java  # Server streaming
│       │       ├── ClientStreamingApplication.java  # Client streaming
│       │       ├── BidirectionalApplication.java    # Bidirectional streaming
│       │       └── common/
│       │           ├── GrpcClient.java              # Connection management
│       │           └── PropertiesHelper.java         # Config utilities
│       ├── proto/
│       │   ├── account-balance.proto
│       │   ├── bank-service.proto
│       │   ├── transfer-service.proto
│       │   └── guess-number.proto
│       └── resources/
│           ├── application.properties
│           └── logback.xml
├── build.gradle
└── settings.gradle
```

## Requirements

- Java 21 (configured via Gradle toolchain)
- Gradle 9.2.1 (included with wrapper)
- **gRPC Server running** on localhost:6565 (see grpc-server module)
- Protocol Buffers 3.25.5 (auto-managed)

## Building

```bash
# Build and generate Protocol Buffer classes
.\gradlew.bat build

# Generate proto classes only
.\gradlew.bat generateProto

# Clean artifacts
.\gradlew.bat clean
```

## Client Applications

### UnaryClient (Default)
**Pattern**: Unary (Request-Response)
**Services**: BankService balance queries

```bash
.\gradlew.bat run
# Demonstrates synchronous and asynchronous balance queries
```

### Server Streaming Client
**Pattern**: Server Streaming (1 Request → Multiple Responses)
**Services**: BankService withdrawal with denomination breakdown

```bash
.\gradlew.bat run --args="ServerStreaming"
# Server sends multiple bill denominations in streaming response
```

### Client Streaming Client
**Pattern**: Client Streaming (Multiple Requests → 1 Response)
**Services**: BankService deposit with multiple amounts

```bash
.\gradlew.bat run --args="ClientStreaming"
# Client streams multiple deposit amounts, server responds with final balance
```

### Bidirectional Streaming Client
**Pattern**: Bidirectional Streaming (Multiple ↔ Multiple)
**Services**: TransferService money transfers, GuessService number game

```bash
.\gradlew.bat run --args="Bidirectional"
# Real-time bidirectional communication for transfers and games
```

## Configuration

**Client Settings** (`src/main/resources/application.properties`):
```properties
host=localhost
grpc.server.port=6565
```

**Connection Management**:
- **Standard channel**: Basic gRPC connection
- **Keep-alive channel**: For long-lived bidirectional streams
- **CountDownLatch**: Coordinates async operations in streaming

## Available Operations

### Banking Operations (BankService)
- **Balance Query** (Unary): Sync/async account balance lookup
- **All Accounts** (Server Streaming): Stream all account information  
- **Withdrawal** (Server Streaming): Request amount, receive bill denominations
- **Deposit** (Client Streaming): Send multiple amounts, get final balance

### Money Transfers (TransferService)
- **Transfer Money** (Bidirectional): Real-time money transfers between accounts
  - Send transfer requests continuously
  - Receive immediate confirmations or errors
  - Requires session affinity for load balancing

### Interactive Gaming (GuessService)
- **Guess Number** (Bidirectional): Number guessing game
  - Send guesses continuously
  - Receive hints (higher/lower) in real-time
  - Demonstrates bidirectional streaming patterns

## Development Notes

### Protocol Buffers
- **Proto synchronization**: Client `.proto` files must match server exactly
- **Generated classes**: Located in `build/generated/source/proto/main/java/`
- **Package mapping**: All proto messages in `com.grpc.course` package
- **DO NOT EDIT** generated classes manually

### Connection Requirements
- **Server dependency**: gRPC server must be running on localhost:6565
- **Keep-alive**: Bidirectional streaming uses keep-alive channels
- **Error handling**: All clients demonstrate proper gRPC error handling
- **Async coordination**: Client streaming uses CountDownLatch for synchronization

### Streaming Best Practices
- **Always complete streams**: Call `onCompleted()` to signal end
- **Handle backpressure**: Don't overwhelm server with rapid requests
- **Error recovery**: Implement proper `onError()` handling
- **Resource cleanup**: Close streams and channels properly
