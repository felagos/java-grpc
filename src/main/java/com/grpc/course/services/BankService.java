package com.grpc.course.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.protobuf.Empty;
import com.grpc.course.AccountBalance;
import com.grpc.course.AllAccountsResponse;
import com.grpc.course.BalanceCheckRequest;
import com.grpc.course.BankServiceGrpc;
import com.grpc.course.Money;
import com.grpc.course.WithdrawRequest;
import com.grpc.course.repository.AccountRepository;
import io.grpc.stub.StreamObserver;

@Service
public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);
    private final AccountRepository accountRepository;

    public BankService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        logger.info("Received request for account number: {}", String.valueOf(request.getAccountNumber()).trim());

        var accountNumber = request.getAccountNumber();
        var balance = accountRepository.getBalance(accountNumber);
        var accountBalance = AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(balance)
                .build();

        logger.info("Sending account balance: {}, for account number: {}", accountBalance.getBalance(), accountBalance.getAccountNumber());

        responseObserver.onNext(accountBalance);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAccounts(Empty request, StreamObserver<AllAccountsResponse> responseObserver) {
        logger.info("Received request for all accounts");

        AllAccountsResponse.Builder responseBuilder = AllAccountsResponse.newBuilder();

        accountRepository.getAllAccounts().forEach((accountNumber, balance) -> {
            var accountBalance = AccountBalance.newBuilder()
                    .setAccountNumber(accountNumber)
                    .setBalance(balance)
                    .build();

            responseBuilder.addAccounts(accountBalance);

            logger.info("Adding account number: {} with balance: {}", accountNumber, balance);
        });

        AllAccountsResponse response = responseBuilder.build();

        logger.info("Sending all accounts response with {} accounts", response.getAccountsCount());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<WithdrawRequest> withdraw(StreamObserver<Money> responseObserver) {
        return super.withdraw(responseObserver);
    }

    

}
