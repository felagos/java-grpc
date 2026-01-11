package com.grpc.course.handlers;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.GuessRequest;
import com.grpc.course.GuessResponse;
import com.grpc.course.Result;

import io.grpc.stub.StreamObserver;

public class GuessRequestHandler implements StreamObserver<GuessRequest> {

    private final Logger logger = LoggerFactory.getLogger(GuessRequestHandler.class);
    private final StreamObserver<GuessResponse> responseObserver;
    private final int secretNumber;
    private int attemptCount = 0;

    public GuessRequestHandler(StreamObserver<GuessResponse> responseObserver) {
        this.responseObserver = responseObserver;
        this.secretNumber = new Random().nextInt(100) + 1; // Random number between 1 and 100
        logger.info("New guessing game started! Secret number generated (1-100)");
    }

    @Override
    public void onNext(GuessRequest request) {
        int guess = request.getGuess();
        attemptCount++;
        
        logger.info("Attempt #{}: Client guessed {}", attemptCount, guess);

        Result result;
        if (guess == secretNumber) {
            result = Result.CORRECT;
            logger.info("Correct! Client guessed the number {} in {} attempts", secretNumber, attemptCount);
        } else if (guess < secretNumber) {
            result = Result.TOO_LOW;
            logger.info("Too low! Secret number is higher than {}", guess);
        } else {
            result = Result.TOO_HIGH;
            logger.info("Too high! Secret number is lower than {}", guess);
        }

        var response = GuessResponse.newBuilder()
                .setAttempt(attemptCount)
                .setResult(result)
                .build();

        responseObserver.onNext(response);

        // If correct, complete the stream
        if (result == Result.CORRECT) {
            logger.info("Game completed! Closing stream.");
            responseObserver.onCompleted();
        }
    }

    @Override
    public void onError(Throwable t) {
        logger.error("Error occurred during guessing game: {}", t.getMessage());
        responseObserver.onError(t);
    }

    @Override
    public void onCompleted() {
        logger.info("Client stopped sending guesses. Game ended after {} attempts.", attemptCount);
        if (attemptCount == 0 || !responseObserver.equals(responseObserver)) {
            responseObserver.onCompleted();
        }
    }
}
