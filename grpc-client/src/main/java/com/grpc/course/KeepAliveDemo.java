package com.grpc.course;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.common.PropertiesHelper;
import com.grpc.course.AccountBalance;
import com.grpc.course.BalanceCheckRequest;
import com.grpc.course.BankServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class KeepAliveDemo {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveDemo.class);

    public static void main(String[] args) {
        Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();
        
        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));

        logger.info("=== gRPC Keep-Alive Configuration Demo ===");
        logger.info("Client Configuration:");
        logger.info("  - keepAliveTime: 10 seconds (sends ping after 10s of inactivity)");
        logger.info("  - keepAliveTimeout: 5 seconds (waits 5s for ping response)");
        logger.info("Server Configuration:");
        logger.info("  - keepAliveTime: 10 seconds");
        logger.info("  - keepAliveTimeout: 1 second");
        logger.info("  - maxConnectionIdle: 25 seconds (closes idle connections after 25s)");
        logger.info("");

        // Demo 1: keepAliveTime behavior
        demonstrateKeepAliveTime(host, port);

        // Demo 2: maxConnectionIdle behavior
        demonstrateMaxConnectionIdle(host, port);

        logger.info("=== Demo completed ===");
    }

    private static void demonstrateKeepAliveTime(String host, int port) {
        logger.info("=== DEMO 1: keepAliveTime Behavior ===");
        logger.info("This demo shows how keepAliveTime works");
        
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .build();

        var stub = BankServiceGrpc.newBlockingStub(channel);

        try {
            // Request 1: Initial connection
            logger.info("Request 1: Establishing initial connection");
            makeBalanceRequest(stub, 7);
            
            // Wait 8 seconds (less than keepAliveTime)
            logger.info("Waiting 8 seconds (less than keepAliveTime of 10s)...");
            Thread.sleep(8000);
            
            // Request 2: No ping needed, connection still fresh
            logger.info("Request 2: Connection is still fresh, no ping was needed");
            makeBalanceRequest(stub, 7);
            
            // Wait 12 seconds (exceeds keepAliveTime)
            logger.info("Waiting 12 seconds (exceeds keepAliveTime of 10s)...");
            logger.info("  -> Client will send a PING to keep connection alive");
            Thread.sleep(12000);
            
            // Request 3: After keep-alive ping
            logger.info("Request 3: After keep-alive ping was sent");
            makeBalanceRequest(stub, 7);
            
            logger.info("DEMO 1 completed successfully");
            logger.info("");
            
        } catch (Exception e) {
            logger.error("Error in keepAliveTime demo", e);
        } finally {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while closing channel");
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void demonstrateMaxConnectionIdle(String host, int port) {
        logger.info("=== DEMO 2: maxConnectionIdle Behavior ===");
        logger.info("This demo shows how server's maxConnectionIdle closes idle connections");
        
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .build();

        var stub = BankServiceGrpc.newBlockingStub(channel);

        try {
            // Request 1: Initial connection
            logger.info("Request 1: Establishing connection");
            makeBalanceRequest(stub, 7);
            
            // Wait 15 seconds (less than maxConnectionIdle of 25s)
            logger.info("Waiting 15 seconds (less than maxConnectionIdle of 25s)...");
            logger.info("  -> Keep-alive pings will be sent, connection stays open");
            Thread.sleep(15000);
            
            // Request 2: Still within maxConnectionIdle
            logger.info("Request 2: Connection is still alive (within maxConnectionIdle limit)");
            makeBalanceRequest(stub, 7);
            
            // Wait 28 seconds (exceeds maxConnectionIdle of 25s)
            logger.info("Waiting 28 seconds (exceeds maxConnectionIdle of 25s)...");
            logger.info("  -> Server will close the connection due to inactivity");
            logger.info("  -> Even though client sends keep-alive pings, server enforces maxConnectionIdle");
            Thread.sleep(28000);
            
            // Request 3: Connection was closed by server, will need to reconnect
            logger.info("Request 3: Attempting request after maxConnectionIdle exceeded");
            logger.info("  -> This should fail or trigger automatic reconnection");
            try {
                makeBalanceRequest(stub, 7);
                logger.info("Request succeeded - gRPC automatically reconnected");
            } catch (StatusRuntimeException e) {
                logger.warn("Request failed as expected: {}", e.getStatus());
                logger.info("Connection was closed by server due to maxConnectionIdle");
                
                // Try one more time - should reconnect
                logger.info("Retrying request - will establish new connection");
                makeBalanceRequest(stub, 7);
                logger.info("Request succeeded after reconnection");
            }
            
            logger.info("DEMO 2 completed successfully");
            logger.info("");
            
        } catch (Exception e) {
            logger.error("Error in maxConnectionIdle demo", e);
        } finally {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while closing channel");
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void makeBalanceRequest(BankServiceGrpc.BankServiceBlockingStub stub, int accountNumber) {
        var request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();
        
        AccountBalance response = stub.getAccountBalance(request);
        logger.info("  ✓ Balance for account {}: ${}", response.getAccountNumber(), response.getBalance());
    }
}
