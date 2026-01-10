package com.grpc.course;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.common.GrpcClient;
import com.grpc.course.common.PropertiesHelper;
import com.google.common.util.concurrent.Uninterruptibles;

public class ClientStreaming {

    private static final Logger logger = LoggerFactory.getLogger(ClientStreaming.class);

    public static void main(String[] args) {
        var accountNumber = 1;

        logger.info("Initiating deposit request for account: {}", accountNumber);

        Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();

        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));

        var client = new GrpcClient(host, port);

        client.deposit(accountNumber, 10, 20, 30, 40, 50);

        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);

        logger.info("Deposit process finished");
    }
}
