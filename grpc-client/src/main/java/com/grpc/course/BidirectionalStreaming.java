package com.grpc.course;

import com.grpc.course.annotation.GrpcClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class BidirectionalStreaming {
    private static final Logger logger = LoggerFactory.getLogger(BidirectionalStreaming.class);

    @GrpcClient("transfer-service")
    private TransferServiceGrpc.TransferServiceStub asyncStub;

    public void run() {
        logger.info("Starting Bidirectional Streaming Client...");
        transfer();
    }

    private void transfer() {
        logger.info("=== Bidirectional Streaming RPC ===");
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<com.grpc.course.TransferResponse> responseObserver = new StreamObserver<com.grpc.course.TransferResponse>() {
            @Override
            public void onNext(com.grpc.course.TransferResponse value) {
                logger.info("Transfer Response - Status: {}, From Transfer Balance: ${}, To Transfer Balance: ${}",
                        value.getStatus(),
                        value.getFromAccount().getBalance(),
                        value.getToAccount().getBalance());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("Transfers completed");
                latch.countDown();
            }
        };

        StreamObserver<TransferRequest> requestObserver = asyncStub.transfer(responseObserver);

        try {
            requestObserver.onNext(TransferRequest.newBuilder()
                    .setFromAccount(1)
                    .setToAccount(2)
                    .setAmount(50)
                    .build());

            requestObserver.onNext(TransferRequest.newBuilder()
                    .setFromAccount(2)
                    .setToAccount(3)
                    .setAmount(25)
                    .build());

            requestObserver.onNext(TransferRequest.newBuilder()
                    .setFromAccount(3)
                    .setToAccount(4)
                    .setAmount(15)
                    .build());

            requestObserver.onCompleted();
            latch.await();
        } catch (Exception e) {
            logger.error("Error during transfer: {}", e.getMessage());
        }
    }
}
