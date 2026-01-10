package com.grpc.course;

import com.grpc.course.common.GrpcServer;
import com.grpc.course.repository.AccountRepository;
import com.grpc.course.services.BankService;
import com.grpc.course.services.TransferService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServerApplication {

	private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);
	private static final int GRPC_PORT = 6565;

	public static void main(String[] args) {
		logger.info("Starting gRPC Console Application...");

		AccountRepository accountRepository = new AccountRepository();
		
		BankService bankService = new BankService(accountRepository);
		TransferService transferService = new TransferService();

		GrpcServer grpcServer = new GrpcServer(List.of(bankService, transferService));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Shutdown hook triggered");
			grpcServer.shutdown();
		}));

		try {
			grpcServer.initServer(GRPC_PORT);
			grpcServer.awaitTermination();
		} catch (Exception e) {
			logger.error("Error running gRPC server", e);
			System.exit(1);
		}
	}
}
