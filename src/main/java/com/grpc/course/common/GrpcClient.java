package com.grpc.course.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.AccountBalance;
import com.grpc.course.BalanceCheckRequest;
import com.grpc.course.BankServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

    private ManagedChannel channel;

    public GrpcClient(String host, int port) {
        initClient(host, port);
    }
    

    private void initClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    public void processBalance(int accountNumber) {
        var stub = BankServiceGrpc.newBlockingStub(channel);
        
        var balanceRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        var accountBalance = stub.getAccountBalance(balanceRequest);

        logger.info("Balance Response - Account Number: {}, Balance: {}",
                accountBalance.getAccountNumber(),
                accountBalance.getBalance());
    }

    public void asyncProcessBalance(int accountNumber) {
        var stub = BankServiceGrpc.newStub(channel);
        
        var balanceRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        stub.getAccountBalance(balanceRequest, createBalanceObserver());
    }

    private StreamObserver<AccountBalance> createBalanceObserver() {
        return new StreamObserver<AccountBalance>() {
            @Override
            public void onNext(AccountBalance value) {
                logger.info("Balance Response - Account Number: {}, Balance: {}",
                        value.getAccountNumber(),
                        value.getBalance());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error fetching balance", t);
            }

            @Override
            public void onCompleted() {
                logger.info("Balance request completed");
            }
        };
    }

}
