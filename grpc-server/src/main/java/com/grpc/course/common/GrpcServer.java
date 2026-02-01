package com.grpc.course.common;

import build.buf.protovalidate.Validator;
import build.buf.protovalidate.ValidatorFactory;
import com.grpc.course.interceptor.ValidationInterceptor;
import com.grpc.course.interceptor.JwtAuthInterceptor;
import com.grpc.course.services.BankService;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    private final Path KEY_STORE_PATH = CertsHelper.KEY_STORE_PATH;
    private final Path TRUST_STORE_PATH = CertsHelper.TRUST_STORE_PATH;

    private Server server;
    private Thread serverThread;

    private final List<BindableService> services;

    public GrpcServer(List<BindableService> services) {
        this.services = services;
        logger.info("{} gRPC service(s) found and will be registered", services.size());
    }

    public void initServer(int port) {
        if (server != null && !server.isShutdown()) {
            logger.warn("gRPC server is already running, shutting down first...");
            shutdown();
        }

        try {
            var serverBuilder = ServerBuilder
                .forPort(port)
                .keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .maxConnectionIdle(25, TimeUnit.SECONDS);

            Validator validator = ValidatorFactory.newBuilder().build();
            ValidationInterceptor validationInterceptor = new ValidationInterceptor(validator);
            JwtAuthInterceptor jwtAuthInterceptor = new JwtAuthInterceptor();

            services.forEach(service -> {
                if (service instanceof BankService) {
                    var serviceWithInterceptors = ServerInterceptors.intercept(
                        service, 
                        jwtAuthInterceptor,   
                        validationInterceptor
                    );
                    serverBuilder.addService(serviceWithInterceptors);
                    logger.info("Registered gRPC service: {} with validation and JWT interceptors", 
                               service.getClass().getSimpleName());
                } else {
                    var serviceWithValidation = ServerInterceptors.intercept(service, validationInterceptor);
                    serverBuilder.addService(serviceWithValidation);
                    logger.info("Registered gRPC service: {} with validation interceptor only", 
                               service.getClass().getSimpleName());
                }
            });

            server = serverBuilder.build().start();

            logger.info("gRPC server is up and running on port {}", port);

            Runnable runnableServer = () -> {
                try {
                    server.awaitTermination();
                } catch (InterruptedException e) {
                    logger.info("gRPC server thread interrupted");
                    Thread.currentThread().interrupt();
                }
            };

            serverThread = new Thread(runnableServer);

            serverThread.setDaemon(false);
            serverThread.setName("grpc-server-thread");
            serverThread.start();

        } catch (Exception e) {
            logger.error("Failed to start gRPC server", e);
            throw new RuntimeException("Failed to start gRPC server", e);
        }
    }

    public void awaitTermination() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void shutdown() {
        if (server != null && !server.isShutdown()) {
            logger.info("Shutting down gRPC server...");
            server.shutdown();

            try {
                if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("gRPC server did not terminate gracefully, forcing shutdown...");
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for gRPC server shutdown");
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
    }

}
