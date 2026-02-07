package com.grpc.course;

import com.grpc.course.annotation.GrpcClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GuessNumberGame {
    private static final Logger logger = LoggerFactory.getLogger(GuessNumberGame.class);

    @GrpcClient("guess-number")
    private GuessNumberGrpc.GuessNumberStub asyncStub;

    private AtomicInteger lowerBound = new AtomicInteger(0);
    private AtomicInteger upperBound = new AtomicInteger(100);

    public void run() {
        logger.info("Starting Guess Number Game Client...");
        playGame();
    }

    private void playGame() {
        logger.info("=== Number Guessing Game ===");
        logger.info("Think of a number between 0 and 100!");

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<com.grpc.course.GuessResponse> responseObserver = new StreamObserver<com.grpc.course.GuessResponse>() {
            @Override
            public void onNext(com.grpc.course.GuessResponse value) {
                logger.info("Attempt: {}, Result: {}", value.getAttempt(), value.getResult());

                if (value.getResult().name().equals("CORRECT")) {
                    latch.countDown();
                } else if (value.getResult().name().equals("TOO_HIGH")) {
                    upperBound.set(value.getAttempt() - 1);
                } else if (value.getResult().name().equals("TOO_LOW")) {
                    lowerBound.set(value.getAttempt() + 1);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("Game completed!");
                latch.countDown();
            }
        };

        StreamObserver<GuessRequest> requestObserver = asyncStub.makeGuess(responseObserver);

        try {
            Scanner scanner = new Scanner(System.in);

            // Initial guess
            int guess = (lowerBound.get() + upperBound.get()) / 2;
            logger.info("My first guess: {}", guess);
            requestObserver.onNext(GuessRequest.newBuilder().setGuess(guess).build());

            // Continue guessing based on responses
            while (latch.getCount() > 0) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    if (input.equalsIgnoreCase("quit")) {
                        requestObserver.onCompleted();
                        break;
                    }

                    if (lowerBound.get() <= upperBound.get()) {
                        guess = (lowerBound.get() + upperBound.get()) / 2;
                        logger.info("My guess: {}", guess);
                        requestObserver.onNext(GuessRequest.newBuilder().setGuess(guess).build());
                    } else {
                        logger.info("Invalid range!");
                        requestObserver.onCompleted();
                        break;
                    }
                }
            }

            latch.await();
            scanner.close();
        } catch (Exception e) {
            logger.error("Error during game: {}", e.getMessage());
        }
    }
}
