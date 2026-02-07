package com.grpc.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            // Default: run UnaryClient
            logger.info("No arguments provided. Running UnaryClient by default.");
            logger.info("Usage: java -jar grpc-client.jar [UnaryClient|ServerStreaming|ClientStreaming|BidirectionalStreaming|WithdrawJWT|KeepAlive|GuessNumber]");
            runUnaryClient(args);
        } else {
            String clientType = args[0];
            switch (clientType) {
                case "ServerStreaming":
                    runServerStreaming(args);
                    break;
                case "ClientStreaming":
                    runClientStreaming(args);
                    break;
                case "BidirectionalStreaming":
                    runBidirectionalStreaming(args);
                    break;
                case "WithdrawJWT":
                    runWithdrawJWT(args);
                    break;
                case "KeepAlive":
                    runKeepAlive(args);
                    break;
                case "GuessNumber":
                    runGuessNumber(args);
                    break;
                case "UnaryClient":
                default:
                    runUnaryClient(args);
                    break;
            }
        }
    }

    private static void runUnaryClient(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        UnaryClient client = context.getBean(UnaryClient.class);
        client.run();
    }

    private static void runServerStreaming(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        ServerStreaming client = context.getBean(ServerStreaming.class);
        client.run();
    }

    private static void runClientStreaming(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        ClientStreaming client = context.getBean(ClientStreaming.class);
        client.run();
    }

    private static void runBidirectionalStreaming(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        BidirectionalStreaming client = context.getBean(BidirectionalStreaming.class);
        client.run();
    }

    private static void runWithdrawJWT(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        WithdrawJwtClient client = context.getBean(WithdrawJwtClient.class);
        client.run();
    }

    private static void runKeepAlive(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        KeepAlive client = context.getBean(KeepAlive.class);
        client.run();
    }

    private static void runGuessNumber(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        GuessNumberGame client = context.getBean(GuessNumberGame.class);
        client.run();
    }
}
