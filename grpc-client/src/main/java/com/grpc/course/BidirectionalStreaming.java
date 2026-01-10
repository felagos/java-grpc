package com.grpc.course;

import java.util.Map;

import com.grpc.course.common.GrpcClient;
import com.grpc.course.common.PropertiesHelper;

public class BidirectionalStreaming {

    public static void main(String[] args) {
         Map<String, String> config = PropertiesHelper.loadPropertiesFromFile();

        String host = config.getOrDefault("host", "localhost");
        int port = Integer.parseInt(config.getOrDefault("grpc.server.port", "6565"));

        var client = new GrpcClient(host, port);

        int[][] transfers = {
            {1001, 2001, 500},
            {2001, 3001, 200},
            {3001, 1001, 100}
        };

        var transferIterator = client.transfer(transfers);
        
        while (transferIterator.hasNext()) {
            var response = transferIterator.next();
            System.out.println("Transfer Status: " + response.getStatus());
            System.out.println("From Account: " + response.getFromAccount().getAccountNumber() + 
                             ", Balance: " + response.getFromAccount().getBalance());
            System.out.println("To Account: " + response.getToAccount().getAccountNumber() + 
                             ", Balance: " + response.getToAccount().getBalance());
            System.out.println("---");
        }

     }

}
