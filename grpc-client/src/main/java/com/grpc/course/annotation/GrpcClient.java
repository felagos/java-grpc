package com.grpc.course.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada para inyectar stubs gRPC en componentes Spring
 * 
 * Ejemplo de uso:
 * @GrpcClient("bank-service")
 * private BankServiceGrpc.BankServiceBlockingStub bankStub;
 * 
 * Con keep-alive:
 * @GrpcClient(value = "bank-service", keepAlive = true)
 * private BankServiceGrpc.BankServiceBlockingStub bankStubAlive;
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcClient {
    /**
     * Nombre del servicio gRPC a conectar
     * Los valores soportados son: bank-service, transfer-service, guess-number
     */
    String value();

    /**
     * Si true, usa el canal con configuración de keep-alive
     * Por defecto es false
     */
    boolean keepAlive() default false;
}
