package com.grpc.course.services;

import com.grpc.course.GuessNumberGrpc;
import com.grpc.course.GuessRequest;
import com.grpc.course.GuessResponse;
import com.grpc.course.handlers.GuessRequestHandler;

import io.grpc.stub.StreamObserver;

public class GuessService extends GuessNumberGrpc.GuessNumberImplBase {

	@Override
	public StreamObserver<GuessRequest> makeGuess(StreamObserver<GuessResponse> responseObserver) {
		return new GuessRequestHandler(responseObserver);
	}

}
