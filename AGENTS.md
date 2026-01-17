# AGENTS.md - Developer Guide for Code Agents

## Build Commands

### Core Build Tasks
```bash
# Build both modules (proto generation + Java compilation)
./gradlew build

# Build specific module
cd grpc-server && ./gradlew build
cd grpc-client && ./gradlew build

# Generate Protocol Buffer code only
./gradlew generateProto

# Run the server
cd grpc-server && ./gradlew run

# Run the client (specific client must be selected)
cd grpc-client && ./gradlew run --args="<client-name>"

# Clean build artifacts
./gradlew clean
```

### Using Makefile (Cross-platform)
```bash
make generate-protos    # Generate all proto files
make server-run         # Start gRPC server on port 6565
make server-stop        # Kill process on port 6565
make help              # Show available commands
```

### Testing
**Note**: Currently no test files exist in the project (`src/test/` directories are empty).

To run tests when added:
```bash
# All tests
./gradlew test

# Single test class
./gradlew test --tests com.grpc.course.YourTestClass

# Single test method
./gradlew test --tests com.grpc.course.YourTestClass.testMethodName

# Tests in a module
cd grpc-server && ./gradlew test
```

## Code Style Guidelines

### Package Structure
- **Main packages**: `com.grpc.course.*`
- **Services**: `com.grpc.course.services`
- **Handlers**: `com.grpc.course.handlers` (streaming logic)
- **Repository**: `com.grpc.course.repository` (data access)
- **Common**: `com.grpc.course.common` (shared utilities like GrpcServer, GrpcClient)
- **Proto messages**: Root package - imported directly (e.g., `import com.grpc.course.AccountBalance`)

### Imports
1. **Order**: `java.*` → `jakarta.*` → third-party (grpc, guava, jackson) → project imports
2. **No wildcard imports** - use explicit imports only
3. **Example from codebase**:
   ```java
   import java.util.concurrent.TimeUnit;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import com.google.common.util.concurrent.Uninterruptibles;
   import com.grpc.course.AccountBalance;
   import io.grpc.stub.StreamObserver;
   ```

### Formatting & Spacing
- **Indentation**: Tabs (or 4 spaces, see existing files)
- **Line length**: No hard limit, but keep under 120 chars when reasonable
- **Braces**: Java K&R style - opening brace on same line
- **Blank lines**: Between methods, between logical sections (1-2 lines max)

### Types & Variables
- **Use `var` keyword** for local variables when type is obvious (follows existing pattern)
  ```java
  var accountNumber = request.getAccountNumber();
  var balance = accountRepository.getBalance(accountNumber);
  ```
- **Logger declaration**: Always static final, one per class
  ```java
  private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
  ```
- **Constants**: UPPER_CASE_WITH_UNDERSCORES
  ```java
  private static final int GRPC_PORT = 6565;
  private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);
  ```

### Naming Conventions
- **Classes**: PascalCase (e.g., `BankService`, `StreamObserver`)
- **Methods**: camelCase (e.g., `getAccountBalance`, `onNext`, `withdraw`)
- **Variables**: camelCase (e.g., `accountNumber`, `balance`, `responseObserver`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `GRPC_PORT`)
- **Package names**: lowercase (e.g., `com.grpc.course.services`)

### Error Handling
1. **Logging errors**: Always log with context
   ```java
   logger.error("Error during deposit for account number: {}", accountNumber, t.getMessage());
   logger.warn("Insufficient balance for account number: {}. Requested: {}, Available: {}",
               accountNumber, amount, balance);
   ```
2. **Exception handling**: Use try-catch at top level, log and exit gracefully
   ```java
   try {
       grpcServer.initServer(GRPC_PORT);
       grpcServer.awaitTermination();
   } catch (Exception e) {
       logger.error("Error running gRPC server", e);
       System.exit(1);
   }
   ```
3. **Null checks**: Use defensive programming
   ```java
   if (amount > balance) {
       logger.warn("Insufficient balance...");
       responseObserver.onCompleted();
       return;
   }
   ```

### Logging Best Practices
- **Logger setup**: One static final logger per class
- **Use placeholders** instead of string concatenation: `logger.info("Amount: {}", amount)`
- **Log levels**:
  - `info()`: Major operations, server start/stop, important state changes
  - `warn()`: Business logic violations (insufficient balance, invalid requests)
  - `error()`: Exceptions, critical failures
- **Method entry/exit**: Not needed for simple methods, use for complex operations

### Proto Files
- **Location**: `src/main/proto/*.proto`
- **Syntax**: Proto3 (`syntax = "proto3";`)
- **Generated code**: NEVER manually edit generated classes in `build/generated/`
- **Regeneration**: Run `./gradlew generateProto` after modifying .proto files
- **Service patterns**: All gRPC services extend `*ImplBase` class
  ```java
  public class BankService extends BankServiceGrpc.BankServiceImplBase {
      @Override
      public void methodName(Request req, StreamObserver<Response> observer) { ... }
  }
  ```

### Streaming Patterns
1. **Unary**: Request-Response pairs, simple method
2. **Server Streaming**: Return `StreamObserver<Request>` from client, call `observer.onNext()` multiple times
3. **Client Streaming**: Return `StreamObserver<Request>`, accumulate in `onNext()`, emit in `onCompleted()`
   ```java
   @Override
   public StreamObserver<DepositRequest> deposit(StreamObserver<AccountBalance> responseObserver) {
       return new StreamObserver<DepositRequest>() {
           @Override
           public void onNext(DepositRequest request) { /* handle */ }
           @Override
           public void onCompleted() { responseObserver.onNext(response); responseObserver.onCompleted(); }
       };
   }
   ```
4. **Bidirectional**: Both sides stream simultaneously

### Common Classes & Patterns
- **GrpcServer**: Initializes server with services, handles port binding, shutdown hooks
- **GrpcClient**: Factory for creating channels and stubs to services
- **StreamObserver**: The callback pattern for async responses (core gRPC abstraction)
- **AccountRepository**: In-memory data storage (key-value maps for accounts)

## Special Notes

### Proto Code Generation
- Proto files must be in `src/main/proto/`
- Generate before Java compilation: `./gradlew generateProto`
- Generated classes appear in `build/generated/source/proto/main/java/`
- Import generated messages from `com.grpc.course` package (configured in build.gradle)
- Never manually edit generated code

### Dependencies
- **gRPC**: 1.72.0 (includes netty, protobuf, stubs)
- **Protocol Buffers**: 3.25.5
- **Java**: 21 (via toolchain)
- **Logging**: SLF4J + Logback
- **Utilities**: Guava (client), Jackson (JSON)

### Server Details
- **Port**: 6565 (configurable in code)
- **Services registered**: BankService, TransferService, GuessService
- **Shutdown**: Graceful shutdown via Runtime shutdown hook
- **Accounts**: 5 pre-initialized accounts with $100-$500 balances

### Debugging
- Enable debug logging by examining log output
- Use `logger.info()` to trace execution flow
- Ensure all gRPC method overrides call `responseObserver.onCompleted()` to signal stream end
- Check `build/generated/` for proto-generated code when imports fail
