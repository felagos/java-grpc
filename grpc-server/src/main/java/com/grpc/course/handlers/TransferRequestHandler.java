package com.grpc.course.handlers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.AccountBalance;
import com.grpc.course.TransferRequest;
import com.grpc.course.TransferResponse;
import com.grpc.course.TransferStatus;
import com.grpc.course.repository.AccountRepository;

import io.grpc.stub.StreamObserver;

public class TransferRequestHandler implements StreamObserver<TransferRequest> {

    private final Logger logger =  LoggerFactory.getLogger(TransferRequestHandler.class);
    private final StreamObserver<TransferResponse> responseObserver;
    private final AccountRepository accountRepository;

    public TransferRequestHandler(StreamObserver<TransferResponse> responseObserver, AccountRepository accountRepository) {
        this.responseObserver = responseObserver;
        this.accountRepository = accountRepository;
    }

	@Override
	public void onNext(TransferRequest value) {
        logger.info("Received transfer request from account: {} to account: {} with amount: {}",
                value.getFromAccount(), value.getToAccount(), value.getAmount());

        var status = getStatus(value);

        if(status == TransferStatus.COMPLETED) {
            logger.info("Transfer completed successfully.");

            var response = TransferResponse.newBuilder()
                    .setStatus(status)
                    .setFromAccount(toAccountBalance(value.getFromAccount()))
                    .setToAccount(toAccountBalance(value.getToAccount()))
                    .setStatus(status)
                    .build();
            
            responseObserver.onNext(response);
            return;
        } 
        
        var response = TransferResponse.newBuilder()
                .setStatus(status)
                .setFromAccount(toAccountBalance(value.getFromAccount()))
                .setToAccount(toAccountBalance(value.getToAccount()))
                .build();

        responseObserver.onNext(response);
	}

	@Override
	public void onError(Throwable t) {
		this.logger.error("Error occurred while processing transfer request: {}", t.getMessage());

        responseObserver.onError(t);
	}

	@Override
	public void onCompleted() {
		this.logger.info("Completed processing transfer requests.");

        responseObserver.onCompleted();
	}

    private TransferStatus getStatus(TransferRequest request) {
        var amount = request.getAmount();
        var fromAccount = request.getFromAccount();
        var toAccount = request.getToAccount();

        if(accountRepository.getBalance(fromAccount) >= amount && (fromAccount != toAccount)) {
            accountRepository.deductBalance(fromAccount, amount);
            accountRepository.addAmount(toAccount, amount);

            return TransferStatus.COMPLETED;
        } 

        return TransferStatus.REJECTED;
    }

    private AccountBalance toAccountBalance(int accountNumber) {
        var balance = accountRepository.getBalance(accountNumber);

        return AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(balance)
                .build();
    }

}
