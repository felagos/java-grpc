package com.grpc.course.services;

import com.grpc.course.TransferRequest;
import com.grpc.course.TransferResponse;
import com.grpc.course.TransferServiceGrpc;
import com.grpc.course.handlers.TransferRequestHandler;
import com.grpc.course.repository.AccountRepository;

import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferRequestHandler(responseObserver, accountRepository);
    }
}
