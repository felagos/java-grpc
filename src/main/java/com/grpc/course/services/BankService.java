package com.grpc.course.services;

import com.grpc.course.AccountBalance;
import com.grpc.course.BankServiceGrpc;

import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {


    @Override
    public void getAccountBalance(com.grpc.course.BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        var accountNumber = request.getAccountNumber();
        var accountBalance = AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(accountNumber * 10)
                .build();

        responseObserver.onNext(accountBalance);
        responseObserver.onCompleted();
    }

}
