package com.grpc.course.services;

import com.grpc.course.TransferRequest;
import com.grpc.course.TransferResponse;
import com.grpc.course.TransferServiceGrpc;

import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {

    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return super.transfer(responseObserver);
    }
}
