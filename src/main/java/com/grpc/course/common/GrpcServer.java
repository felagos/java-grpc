package com.grpc.course.common;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

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
            var serverBuilder = ServerBuilder.forPort(port);

            services.forEach(service -> {
                serverBuilder.addService(service);
                logger.info("Registered gRPC service: {}", service.getClass().getSimpleName());
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

            serverThread.setDaemon(true);
            serverThread.setName("grpc-server-thread");
            serverThread.start();

        } catch (Exception e) {
            logger.error("Failed to start gRPC server", e);
            throw new RuntimeException("Failed to start gRPC server", e);
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
