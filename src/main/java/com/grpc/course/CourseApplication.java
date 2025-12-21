package com.grpc.course;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.grpc.course.common.GrpcClient;
import com.grpc.course.common.GrpcServer;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class CourseApplication {

	private final GrpcServer grpcServer;

	@Value("${grpc.server.port}")
	private int grpcPort;

	public CourseApplication(GrpcServer grpcServer) {
		this.grpcServer = grpcServer;
	}

	public static void main(String[] args) {
		SpringApplication.run(CourseApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			grpcServer.initServer(grpcPort);
		};
	}

	@PreDestroy
	public void onShutdown() {
		grpcServer.shutdown();
	}
}