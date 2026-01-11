package com.grpc.course;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.common.PropertiesHelper;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GuessNumberGame {

    private static final Logger logger = LoggerFactory.getLogger(GuessNumberGame.class);

    public static void main(String[] args) {
        Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();

        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        var stub = GuessNumberGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        // Binary search bounds using atomic integers for thread-safe updates
        AtomicInteger low = new AtomicInteger(1);
        AtomicInteger high = new AtomicInteger(100);
        AtomicInteger lastGuess = new AtomicInteger(0);

        try {
            logger.info("🎮 Starting Guess Number Game with Binary Search Strategy (1-100)...");
            logger.info("═══════════════════════════════════════════════════════════════");

            @SuppressWarnings("unchecked")
            final StreamObserver<GuessRequest>[] requestObserverHolder = new StreamObserver[1];

            var responseObserver = new StreamObserver<GuessResponse>() {
                @Override
                public void onNext(GuessResponse response) {
                    int attempt = response.getAttempt();
                    Result result = response.getResult();
                    int guess = lastGuess.get();

                    logger.info("📨 Attempt #{}: Guess={}, Result={}", attempt, guess, result);

                    if (result == Result.CORRECT) {
                        logger.info("🎉 SUCCESS! Found the number {} in {} attempts!", guess, attempt);
                        requestObserverHolder[0].onCompleted();
                        latch.countDown();
                    } else if (result == Result.TOO_LOW) {
                        // Adjust binary search: number is higher
                        low.set(guess + 1);
                        logger.info("⬆️  Too low! Adjusting range: [{}, {}]", low.get(), high.get());
                        sendNextGuess();
                    } else if (result == Result.TOO_HIGH) {
                        // Adjust binary search: number is lower
                        high.set(guess - 1);
                        logger.info("⬇️  Too high! Adjusting range: [{}, {}]", low.get(), high.get());
                        sendNextGuess();
                    }
                }

                private void sendNextGuess() {
                    if (low.get() <= high.get()) {
                        int nextGuess = low.get() + (high.get() - low.get()) / 2;
                        lastGuess.set(nextGuess);

                        logger.info("📤 Sending guess: {}", nextGuess);

                        var request = GuessRequest.newBuilder()
                                .setGuess(nextGuess)
                                .build();

                        requestObserverHolder[0].onNext(request);
                    } else {
                        logger.warn("No valid range remaining, ending game");
                        requestObserverHolder[0].onCompleted();
                        latch.countDown();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("❌ Error during guessing game", t);
                    latch.countDown();
                }

                @Override
                public void onCompleted() {
                    logger.info("✅ Server completed the guessing game");
                    latch.countDown();
                }
            };

            var requestObserver = stub.makeGuess(responseObserver);
            requestObserverHolder[0] = requestObserver;

            // Send first guess
            int firstGuess = low.get() + (high.get() - low.get()) / 2;
            lastGuess.set(firstGuess);

            logger.info("📤 Sending initial guess: {}", firstGuess);

            var request = GuessRequest.newBuilder()
                    .setGuess(firstGuess)
                    .build();

            requestObserver.onNext(request);

            // Wait for game to complete
            if (!latch.await(30, TimeUnit.SECONDS)) {
                logger.warn("⏱️  Game timeout after 30 seconds");
                requestObserver.onCompleted();
            }

            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("🏁 Game finished!");

        } catch (Exception e) {
            logger.error("Error during guessing game", e);
        } finally {
            try {
                channel.shutdown();
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
