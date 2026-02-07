package com.grpc.course;

import com.grpc.course.annotation.GrpcClient;
import com.grpc.course.BankServiceGrpc;
import com.grpc.course.WithdrawRequest;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServerStreaming {
    private static final Logger logger = LoggerFactory.getLogger(ServerStreaming.class);

    @GrpcClient("bank-service")
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;

    public void run() {
        logger.info("Starting Server Streaming Client...");
        processWithdraw();
    }

    private void processWithdraw() {
        logger.info("=== Server Streaming RPC ===");
        try {
            WithdrawRequest request = WithdrawRequest.newBuilder()
                    .setAccountNumber(1)
                    .setAmount(10)
                    .build();

            var response = blockingStub.withdraw(request);
            logger.info("Withdrawing $10 from Account 1:");
            while (response.hasNext()) {
                var money = response.next();
                logger.info("Received: ${}", money.getAmount());
            }
        } catch (StatusRuntimeException e) {
            logger.error("Error: {}", e.getStatus());
        }
    }
}
