package com.grpc.course;

import com.grpc.course.annotation.GrpcClient;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeepAlive {
    private static final Logger logger = LoggerFactory.getLogger(KeepAlive.class);

    @GrpcClient(value = "bank-service", keepAlive = true)
    private BankServiceGrpc.BankServiceBlockingStub blockingStubAlive;

    public void run() {
        logger.info("Starting Keep-Alive Client...");
        demonstrateKeepAliveTime();
        demonstrateMaxConnectionIdle();
    }

    private void demonstrateKeepAliveTime() {
        logger.info("=== Demonstrating Keep-Alive Time ===");
        logger.info("Configured: keepAliveTime=10s, keepAliveTimeout=1s");

        try {
            for (int i = 0; i < 3; i++) {
                logger.info("Call {}: Getting account balance...", i + 1);
                BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                        .setAccountNumber(i + 1)
                        .build();

                var response = blockingStubAlive.getAccountBalance(request);
                logger.info("Balance for Account {}: ${}", i + 1, response.getBalance());

                if (i < 2) {
                    logger.info("Sleeping for 5 seconds...");
                    Thread.sleep(5000);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted: {}", e.getMessage());
        } catch (StatusRuntimeException e) {
            logger.error("Error: {}", e.getStatus());
        }
    }

    private void demonstrateMaxConnectionIdle() {
        logger.info("\n=== Demonstrating Max Connection Idle ===");
        logger.info("Configured: maxConnectionIdle=25s");

        try {
            logger.info("Making a call to reset idle timer...");
            BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(1)
                    .build();

            var response = blockingStubAlive.getAccountBalance(request);
            logger.info("Balance: ${}", response.getBalance());

            logger.info("Idle connection will be closed after 25 seconds of no activity");
            logger.info("Sleeping for 10 seconds...");
            Thread.sleep(10000);

            logger.info("Making another call...");
            response = blockingStubAlive.getAccountBalance(request);
            logger.info("Balance: ${}", response.getBalance());
            logger.info("Connection is still alive!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted: {}", e.getMessage());
        } catch (StatusRuntimeException e) {
            logger.error("Error: {}", e.getStatus());
        }
    }
}
