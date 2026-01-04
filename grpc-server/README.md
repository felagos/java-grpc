# gRPC Server Application

Servidor gRPC que implementa un servicio bancario con operaciones de consulta de balance, retiro y depósito.

## Estructura del Proyecto

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
│       │       └── services/
│       │           └── BankService.java
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

## Compilación

Para compilar el proyecto y generar las clases desde los archivos proto:

```bash
.\gradlew.bat build
```

## Ejecución

Para ejecutar el servidor gRPC:

```bash
.\gradlew.bat run
```

El servidor se iniciará en el puerto **6565** por defecto.

## Servicios gRPC Disponibles

### BankService

- `GetAccountBalance`: Consulta el balance de una cuenta
- `GetAllAccounts`: Obtiene todas las cuentas
- `Withdraw`: Retira dinero de una cuenta (server streaming)
- `Deposit`: Deposita dinero en una cuenta (client streaming)

## Configuración

La configuración del servidor se encuentra en `src/main/resources/application.properties`:

```properties
grpc.server.port=6565
```

## Notas de Desarrollo

- Los archivos `.proto` están en `src/main/proto/`
- Las clases Java generadas están en `build/generated/source/proto/main/`
- **No editar** las clases generadas automáticamente
- Después de modificar archivos `.proto`, ejecutar `.\gradlew.bat generateProto`
