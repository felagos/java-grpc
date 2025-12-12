package com.grpc.course.common;

import com.grpc.course.services.BankService;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);
    private static Server server;

    public static void initServer(int port) throws Exception {
        if (server != null && !server.isShutdown()) {
            logger.warn("gRPC server is already running, shutting down first...");
            server.shutdown();
        }
        
        server = ServerBuilder.forPort(port)
                .addService(new BankService())
                .build()
                .start();
        
        logger.info("gRPC server is up and running on port {}", port);
        
        server.awaitTermination();
    }

    public static void shutdown() {
        if (server != null && !server.isShutdown()) {
            logger.info("Shutting down gRPC server...");
            server.shutdown();
        }
    }

}
