package com.grpc.course.annotation;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.grpc.course.BankServiceGrpc;
import com.grpc.course.GuessNumberGrpc;
import com.grpc.course.TransferServiceGrpc;

/**
 * BeanPostProcessor que procesa la anotación @GrpcClient
 * Inyecta automáticamente los stubs gRPC en los campos anotados
 */
@Component
public class GrpcClientInjector implements BeanPostProcessor {

    private final BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;
    private final BankServiceGrpc.BankServiceStub bankAsyncStub;
    private final BankServiceGrpc.BankServiceBlockingStub bankBlockingStubAlive;
    private final TransferServiceGrpc.TransferServiceBlockingStub transferBlockingStub;
    private final TransferServiceGrpc.TransferServiceStub transferAsyncStub;
    private final GuessNumberGrpc.GuessNumberBlockingStub guessBlockingStub;
    private final GuessNumberGrpc.GuessNumberStub guessAsyncStub;

    public GrpcClientInjector(
            BankServiceGrpc.BankServiceBlockingStub bankBlockingStub,
            BankServiceGrpc.BankServiceStub bankAsyncStub,
            BankServiceGrpc.BankServiceBlockingStub bankBlockingStubAlive,
            TransferServiceGrpc.TransferServiceBlockingStub transferBlockingStub,
            TransferServiceGrpc.TransferServiceStub transferAsyncStub,
            GuessNumberGrpc.GuessNumberBlockingStub guessBlockingStub,
            GuessNumberGrpc.GuessNumberStub guessAsyncStub) {
        this.bankBlockingStub = bankBlockingStub;
        this.bankAsyncStub = bankAsyncStub;
        this.bankBlockingStubAlive = bankBlockingStubAlive;
        this.transferBlockingStub = transferBlockingStub;
        this.transferAsyncStub = transferAsyncStub;
        this.guessBlockingStub = guessBlockingStub;
        this.guessAsyncStub = guessAsyncStub;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            GrpcClient annotation = field.getAnnotation(GrpcClient.class);
            
            if (annotation != null) {
                field.setAccessible(true);
                try {
                    Object stub = getStubForService(annotation.value(), field.getType(), annotation.keepAlive());
                    field.set(bean, stub);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject gRPC stub for service: " + annotation.value(), e);
                }
            }
        }
        
        return bean;
    }

    /**
     * Retorna el stub gRPC apropiado basado en el nombre del servicio y tipo de campo
     */
    private Object getStubForService(String serviceName, Class<?> fieldType, boolean keepAlive) {
        return switch (serviceName) {
            case "bank-service" -> getStubForType(fieldType, bankBlockingStub, bankAsyncStub, bankBlockingStubAlive, keepAlive);
            case "transfer-service" -> getStubForType(fieldType, transferBlockingStub, transferAsyncStub, null, keepAlive);
            case "guess-number" -> getStubForType(fieldType, guessBlockingStub, guessAsyncStub, null, keepAlive);
            default -> throw new IllegalArgumentException("Unknown gRPC service: " + serviceName);
        };
    }

    /**
     * Selecciona entre stub blocking o async según el tipo de campo, y si usa keep-alive
     */
    private Object getStubForType(Class<?> fieldType, Object blockingStub, Object asyncStub, Object blockingStubAlive, boolean keepAlive) {
        if (fieldType.getName().contains("Blocking")) {
            return keepAlive && blockingStubAlive != null ? blockingStubAlive : blockingStub;
        } else if (fieldType.getName().contains("Stub")) {
            return asyncStub;
        }
        throw new IllegalArgumentException("Unsupported stub type: " + fieldType.getName());
    }
}

