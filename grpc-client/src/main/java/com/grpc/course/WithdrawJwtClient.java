package com.grpc.course;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.common.GrpcClient;
import com.grpc.course.common.PropertiesHelper;

public class WithdrawJwtClient {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawJwtClient.class);

    public static void main(String[] args) {
        Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();
        
        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));
        int accountNumber = 1;
        int amount = 50;

        logger.info("Starting gRPC Client for JWT-protected withdraw - Host: {}, Port: {}", host, port);

        var client = new GrpcClient(host, port);

        // Test 1: Withdraw sin JWT (debería fallar)
        logger.info("===== TEST 1: Withdraw without JWT (should fail) =====");
        client.processWithdraw(accountNumber, amount);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Test 2: Withdraw con JWT inválido (debería fallar)
        logger.info("===== TEST 2: Withdraw with invalid JWT (should fail) =====");
        client.processWithdrawWithJwt(accountNumber, amount, "Bearer invalid-token");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Test 3: Withdraw con JWT válido (debería funcionar)
        logger.info("===== TEST 3: Withdraw with valid JWT (should succeed) =====");
        client.processWithdrawWithJwt(accountNumber, amount, "Bearer valid-jwt-token-12345");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Test 4: Verificar que otros métodos NO requieren JWT
        logger.info("===== TEST 4: Balance check without JWT (should work) =====");
        client.processBalance(accountNumber);
        
        logger.info("JWT Withdraw Client execution completed");
    }
}