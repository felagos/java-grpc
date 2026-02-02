# gRPC Shared Proto Definitions

This module contains the shared Protocol Buffer (`.proto`) definitions used by both `grpc-server` and `grpc-client`.

## Overview

Instead of duplicating proto files across modules, all proto definitions are centralized here and referenced as a dependency by:
- `grpc-server`
- `grpc-client`

## Proto Files

### account-balance.proto
Defines the `AccountBalance` message that represents account information.

### bank-service.proto
Defines the `BankService` gRPC service with operations for:
- `GetAccountBalance` - Unary RPC
- `GetAllAccounts` - Unary RPC
- `Withdraw` - Server streaming RPC
- `Deposit` - Client streaming RPC

### transfer-service.proto
Defines the `TransferService` gRPC service with:
- `Transfer` - Bidirectional streaming RPC for money transfers

### guess-number.proto
Defines the `GuessNumber` gRPC service for a number guessing game with:
- `MakeGuess` - Bidirectional streaming RPC

## Generated Code

Running `./gradlew build` will automatically generate Java classes from the proto files into:
```
build/generated/source/proto/main/java/com/grpc/course/
```

These generated classes are available to both consuming modules via the project dependency.

## Building

Build the shared module:
```bash
./gradlew :grpc-shared:build
```

Or build all modules (including this one):
```bash
./gradlew build
```

## Dependency Configuration

Both `grpc-server` and `grpc-client` declare this module as a dependency:

```gradle
dependencies {
    implementation project(':grpc-shared')
}
```

This ensures they have access to all generated gRPC stubs and message classes.
