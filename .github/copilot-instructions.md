# Copilot Instructions for java-grpc Project

## Project Overview
Spring Boot 4.0.0 application with gRPC integration using Protocol Buffers. The project uses Gradle for builds with Protobuf code generation.

## Architecture & Key Components

### Generated Code Structure
- **Proto files**: `src/main/proto/*.proto` - Define message schemas and gRPC services
- **Generated Java**: `build/generated/source/proto/main/java/` - Auto-generated from proto files (DO NOT EDIT)
- **Generated gRPC stubs**: `build/generated/source/proto/main/grpc/` - Auto-generated service stubs

### Core Dependencies
- **gRPC**: v1.72.0 (netty-shaded, protobuf, stub)
- **Protocol Buffers**: v3.25.5
- **Java**: Version 21 (configured via toolchain)
- **Spring Boot**: v4.0.0

## Critical Developer Workflows

### Building the Project
```bash
# Windows (PowerShell)
.\gradlew.bat build

# This triggers proto compilation BEFORE Java compilation
# Generated classes appear in build/generated/source/proto/main/
```

### Proto File Changes
When modifying `.proto` files:
1. Run `.\gradlew.bat generateProto` to regenerate Java classes
2. Generated classes are in `build/generated/source/proto/main/java/`
3. Import generated classes with their outer class name (e.g., `PersonOuterClass.Person`)

### Running the Application
```bash
.\gradlew.bat bootRun
```

## Project-Specific Conventions

### Package Structure
- Main application: `com.grpc.course`
- Proto package: Root package (no package declaration in `person.proto`)
- Generated classes: Top-level package (PersonOuterClass)

### Proto Message Pattern
Proto files generate outer classes containing message definitions:
```java
// From person.proto -> PersonOuterClass.java
PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
    .setName("John")
    .setAge(30)
    .build();
```

### Gradle Plugin Configuration
The `com.google.protobuf` plugin (v0.9.4) automatically:
- Compiles `.proto` files during build
- Generates both message classes and gRPC service stubs
- Integrates with sourceSets for IDE recognition

## Adding New Features

### Creating gRPC Services
1. Define service in `.proto` file with `service` keyword
2. Run `.\gradlew.bat generateProto`
3. Implement generated service base class (e.g., `PersonServiceGrpc.PersonServiceImplBase`)
4. Register service with gRPC server

### Adding Proto Messages
1. Create/modify `.proto` files in `src/main/proto/`
2. Use proto3 syntax
3. Run gradle build to generate Java classes
4. Never manually edit generated classes

## Common Pitfalls

### Build Order
Always run proto generation before Java compilation fails. The Gradle build handles this automatically via `generateProtoTasks`, but manual compilation requires explicit proto generation first.

### Import Paths
Generated proto classes are in the default package. Import with outer class name:
```java
import PersonOuterClass.Person;  // Correct
```

### Gradle Wrapper
Use `.\gradlew.bat` (Windows) not `gradle` - ensures consistent Gradle 9.2.1 usage across environments.

## Key Files to Reference
- `build.gradle` - Dependency versions and protobuf plugin configuration
- `src/main/proto/person.proto` - Example proto message definition
- `gradle/wrapper/gradle-wrapper.properties` - Gradle version pinning
