package com.grpc.course;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.common.GrpcClient;
import com.grpc.course.common.PropertiesHelper;

public class UnaryClient {

    private static final Logger logger = LoggerFactory.getLogger(UnaryClient.class);

    public static void main(String[] args) {
        Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();
        
        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));
        int accountNumber = 7;

        logger.info("Starting gRPC Client - Host: {}, Port: {}", host, port);

        var client = new GrpcClient(host, port);

        logger.info("Performing synchronous balance check for account: {}", accountNumber);
        client.processBalance(accountNumber);

        logger.info("Performing asynchronous balance check for account: {}", accountNumber);
        client.asyncProcessBalance(accountNumber);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Client interrupted", e);
        }
        
        logger.info("Client execution completed");
    }
}
