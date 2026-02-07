package com.grpc.course;

import net.devh.boot.grpc.client.inject.GrpcClient;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class UnaryClient {
    private static final Logger logger = LoggerFactory.getLogger(UnaryClient.class);

    @GrpcClient("bank-service")
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;

    @GrpcClient("bank-service")
    private BankServiceGrpc.BankServiceStub asyncStub;

    public void run() {
        logger.info("Starting Unary Client...");
        processBalance();
        asyncProcessBalance();
    }

    private void processBalance() {
        logger.info("=== Unary RPC (Blocking) ===");
        try {
            BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(1)
                    .build();

            var response = blockingStub.getAccountBalance(request);
            logger.info("Balance for Account 1: ${}", response.getBalance());
        } catch (StatusRuntimeException e) {
            logger.error("Error: {}", e.getStatus());
        }
    }

    private void asyncProcessBalance() {
        logger.info("\n=== Unary RPC (Async) ===");

        BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(2)
                .build();

        asyncStub.getAccountBalance(request, new StreamObserver<AccountBalance>() {
            @Override
            public void onNext(AccountBalance value) {
                logger.info("Balance for Account 2: ${}", value.getBalance());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("Call completed");
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
