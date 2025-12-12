package com.grpc.course.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.AccountBalance;
import com.grpc.course.BalanceCheckRequest;
import com.grpc.course.BankServiceGrpc;

import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        logger.info("Received request for account number: {}", String.valueOf(request.getAccountNumber()).trim());

        var accountNumber = request.getAccountNumber();
        var accountBalance = AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(accountNumber * 10)
                .build();

        logger.info("Sending account balance: {}, for account number: {}", accountBalance.getBalance(), accountBalance.getAccountNumber());

        responseObserver.onNext(accountBalance);
        responseObserver.onCompleted();
    }

}
