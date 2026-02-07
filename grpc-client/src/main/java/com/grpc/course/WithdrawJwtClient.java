package com.grpc.course;

import com.grpc.course.annotation.GrpcClient;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WithdrawJwtClient {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawJwtClient.class);

    @GrpcClient("bank-service")
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;

    public void run() {
        logger.info("Starting Withdraw JWT Client...");
        processWithdrawWithoutJwt();
        processWithdrawWithJwt();
    }

    private void processWithdrawWithoutJwt() {
        logger.info("=== Withdraw Without JWT ===");
        try {
            WithdrawRequest request = WithdrawRequest.newBuilder()
                    .setAccountNumber(1)
                    .setAmount(50)
                    .build();

            var response = blockingStub.withdraw(request);
            while (response.hasNext()) {
                var money = response.next();
                logger.info("Received: ${}", money.getAmount());
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.UNAUTHENTICATED) {
                logger.info("Unauthenticated - JWT required");
            } else {
                logger.error("Error: {}", e.getStatus());
            }
        }
    }

    private void processWithdrawWithJwt() {
        logger.info("\n=== Withdraw With JWT ===");
        try {
            WithdrawRequest request = WithdrawRequest.newBuilder()
                    .setAccountNumber(1)
                    .setAmount(50)
                    .build();

            Metadata headers = new Metadata();
            Metadata.Key<String> authKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(authKey, "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIn0.test");

            var stub = blockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
            var response = stub.withdraw(request);
            while (response.hasNext()) {
                var money = response.next();
                logger.info("Received: ${}", money.getAmount());
            }
        } catch (StatusRuntimeException e) {
            logger.error("Error: {}", e.getStatus());
        }
    }
}
