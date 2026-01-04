# gRPC Client Application

Cliente gRPC que se conecta al servidor bancario para realizar operaciones de consulta de balance, retiro y depósito.

## Estructura del Proyecto

```
grpc-client/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/grpc/course/
│       │       ├── ClientApplication.java
│       │       ├── ServerStreamingApplication.java
│       │       ├── ClientStreamingApplication.java
│       │       └── common/
│       │           ├── GrpcClient.java
│       │           └── PropertiesHelper.java
│       ├── proto/
│       │   └── bank-service.proto
│       └── resources/
│           └── application.properties
├── build.gradle
└── settings.gradle
```

## Requisitos

- Java 21
- Gradle 9.2.1 (incluido con el wrapper)
- Servidor gRPC ejecutándose en localhost:6565

## Compilación

Para compilar el proyecto y generar las clases desde los archivos proto:

```bash
.\gradlew.bat build
```

## Aplicaciones Cliente

### ClientApplication

Realiza consultas síncronas y asíncronas de balance de cuenta.

```bash
.\gradlew.bat run
```

### ServerStreamingApplication

Realiza una operación de retiro (el servidor envía múltiples respuestas en streaming).

```bash
.\gradlew.bat run --args="com.grpc.course.ServerStreamingApplication"
```

O modificar `mainClass` en `build.gradle` temporalmente.

### ClientStreamingApplication

Realiza una operación de depósito (el cliente envía múltiples peticiones en streaming).

```bash
.\gradlew.bat run --args="com.grpc.course.ClientStreamingApplication"
```

## Configuración

La configuración del cliente se encuentra en `src/main/resources/application.properties`:

```properties
host=localhost
grpc.server.port=6565
```

## Operaciones Disponibles

### Consulta de Balance
- **Síncrona**: Consulta el balance y espera la respuesta
- **Asíncrona**: Consulta el balance con callback

### Retiro (Server Streaming)
- El cliente solicita un retiro
- El servidor envía múltiples billetes en streaming

### Depósito (Client Streaming)
- El cliente envía múltiples cantidades en streaming
- El servidor responde con el balance final

## Notas de Desarrollo

- Los archivos `.proto` deben ser idénticos a los del servidor
- Las clases Java generadas están en `build/generated/source/proto/main/`
- **No editar** las clases generadas automáticamente
- El cliente requiere que el servidor esté corriendo para funcionar
