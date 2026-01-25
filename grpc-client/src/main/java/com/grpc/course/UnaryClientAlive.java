package com.grpc.course;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.common.GrpcClient;
import com.grpc.course.common.PropertiesHelper;

public class UnaryClientAlive {

    private static final Logger logger = LoggerFactory.getLogger(UnaryClientAlive.class);

    public static void main(String[] args) {
        Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();
        
        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));
        int accountNumber = 7;

        logger.info("Starting gRPC Client with Keep-Alive - Host: {}, Port: {}", host, port);

        var client = new GrpcClient(host, port);

        logger.info("Performing balance check with keep-alive channel for account: {}", accountNumber);
        client.processBalanceAlive(accountNumber);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Client interrupted", e);
        }
        
        logger.info("Client execution completed");
    }
}
