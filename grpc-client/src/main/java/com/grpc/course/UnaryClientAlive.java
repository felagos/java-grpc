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
        logger.info("Keep-Alive Configuration: Time=10s, Timeout=5s");

        var client = new GrpcClient(host, port);

        try {
            // Primera llamada - establece la conexión
            logger.info("=== Request 1: Initial connection ===");
            client.processBalanceAlive(accountNumber);
            
            // Espera 6 segundos (menor a keepAliveTime de 10s)
            logger.info("Waiting 6 seconds (less than keepAliveTime)...");
            Thread.sleep(6000);
            
            // Segunda llamada - la conexión sigue viva sin ping
            logger.info("=== Request 2: Connection still active (no ping needed) ===");
            client.processBalanceAlive(accountNumber);
            
            // Espera 12 segundos (mayor a keepAliveTime de 10s)
            logger.info("Waiting 12 seconds (exceeds keepAliveTime - ping will be sent)...");
            Thread.sleep(12000);
            
            // Tercera llamada - debería haber enviado un keep-alive ping
            logger.info("=== Request 3: After keep-alive ping ===");
            client.processBalanceAlive(accountNumber);
            
            // Espera 7 segundos
            logger.info("Waiting 7 seconds...");
            Thread.sleep(7000);
            
            // Cuarta llamada
            logger.info("=== Request 4: Another request ===");
            client.processBalanceAlive(accountNumber);
            
            // Simula uso continuo con múltiples llamadas
            logger.info("=== Simulating continuous usage with multiple requests ===");
            for (int i = 1; i <= 5; i++) {
                logger.info("Request {}/5", i);
                client.processBalanceAlive(accountNumber);
                Thread.sleep(3000); // 3 segundos entre cada llamada
            }
            
            // Espera larga para forzar keep-alive ping
            logger.info("Waiting 15 seconds (long idle period - multiple pings may be sent)...");
            Thread.sleep(15000);
            
            // Llamada final después de período de inactividad
            logger.info("=== Final request after long idle period ===");
            client.processBalanceAlive(accountNumber);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Client interrupted", e);
        }
        
        logger.info("Client execution completed - Connection maintained throughout all requests");
    }
}
