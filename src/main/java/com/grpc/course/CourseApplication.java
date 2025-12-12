package com.grpc.course;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.grpc.course.common.GrpcServer;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class CourseApplication {

	@Value("${grpc.server.port}")
	private int grpcPort;

	public static void main(String[] args) {
		SpringApplication.run(CourseApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			GrpcServer.initServer(grpcPort);
		};
	}

	@PreDestroy
	public void onShutdown() {
		GrpcServer.shutdown();
	}
}