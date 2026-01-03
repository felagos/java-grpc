package com.grpc.course;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.grpc.course.common.GrpcClient;
import com.grpc.course.common.PropertiesHelper;

public class ServerStreamingApplication {

    private static final Logger logger = LoggerFactory.getLogger(ServerStreamingApplication.class);

    public static void main(String[] args) {
        var accountNumber = 1;
        var amount = 100;

        logger.info("Initiating withdraw request for account: {} with amount: {}", accountNumber, amount);

        Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();

        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));

        var client = new GrpcClient(host, port);

        var response = client.withdraw(accountNumber, amount);

        while(response.hasNext()) {
            var money = response.next();
            logger.info("Received money: {}", money);
        }
    }
}
