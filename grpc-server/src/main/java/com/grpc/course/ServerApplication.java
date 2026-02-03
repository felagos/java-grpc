package com.grpc.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {

	private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

	public static void main(String[] args) {
		logger.info("Starting gRPC Spring Boot Application...");
		SpringApplication.run(ServerApplication.class, args);
	}
}
