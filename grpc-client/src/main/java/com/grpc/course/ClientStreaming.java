package com.grpc.course;

import net.devh.boot.grpc.client.inject.GrpcClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Lazy
public class ClientStreaming {
    private static final Logger logger = LoggerFactory.getLogger(ClientStreaming.class);

    @GrpcClient("bank-service")
    private BankServiceGrpc.BankServiceStub asyncStub;

    public void run() {
        logger.info("Starting Client Streaming Client...");
        deposit();
    }

    private void deposit() {
        logger.info("=== Client Streaming RPC ===");
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AccountBalance> responseObserver = new StreamObserver<AccountBalance>() {
            @Override
            public void onNext(AccountBalance value) {
                logger.info("Deposit Response - New Balance: ${}", value.getBalance());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("Deposits completed");
                latch.countDown();
            }
        };

        StreamObserver<DepositRequest> requestObserver = asyncStub.deposit(responseObserver);

        try {
            requestObserver.onNext(DepositRequest.newBuilder()
                    .setAccountNumber(1)
                    .build());

            requestObserver.onNext(DepositRequest.newBuilder()
                    .setMoney(Money.newBuilder().setAmount(50).build())
                    .build());

            requestObserver.onNext(DepositRequest.newBuilder()
                    .setMoney(Money.newBuilder().setAmount(75).build())
                    .build());

            requestObserver.onCompleted();
            latch.await();
        } catch (Exception e) {
            logger.error("Error during deposit: {}", e.getMessage());
        }
    }
}
