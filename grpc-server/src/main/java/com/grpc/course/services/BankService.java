package com.grpc.course.services;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import com.grpc.course.AccountBalance;
import com.grpc.course.AllAccountsResponse;
import com.grpc.course.BalanceCheckRequest;
import com.grpc.course.BankServiceGrpc;
import com.grpc.course.DepositRequest;
import com.grpc.course.Money;
import com.grpc.course.WithdrawRequest;
import com.grpc.course.repository.AccountRepository;
import com.grpc.course.validator.RequestValidator;

import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);
    private final AccountRepository accountRepository;

    public BankService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private boolean handleGetAccountBalanceError(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        var isValidAccountError = RequestValidator.validateAccount(request.getAccountNumber());
        var isValidAmountError = RequestValidator.isAmountDivisibleBy10(request.getAccountNumber());

        if (isValidAccountError.isPresent()) {
            logger.error("Validation error for account number: {}: {}", request.getAccountNumber(),
                    isValidAccountError.get().getDescription());
            responseObserver.onError(isValidAccountError.get().asRuntimeException());
            return false;
        }

        if (isValidAmountError.isPresent()) {
            logger.error("Validation error for amount: {}: {}", request.getAccountNumber(),
                    isValidAmountError.get().getDescription());
            responseObserver.onError(isValidAmountError.get().asRuntimeException());
            return false;
        }

        return true;
    }

    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        logger.info("Received request for account number: {}", String.valueOf(request.getAccountNumber()).trim());

        var isValidRequest = this.handleGetAccountBalanceError(request, responseObserver);

        if (!isValidRequest) {
            return;
        }

        var accountNumber = request.getAccountNumber();
        var balance = accountRepository.getBalance(accountNumber);
        var accountBalance = AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(balance)
                .build();

        logger.info("Sending account balance: {}, for account number: {}", accountBalance.getBalance(),
                accountBalance.getAccountNumber());

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
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        logger.info("===== WITHDRAW METHOD CALLED =====");
        logger.info("Received withdraw request for account number: {} with amount: {}", request.getAccountNumber(),
                request.getAmount());

        var accountNumber = request.getAccountNumber();
        var amount = request.getAmount();

        var balance = accountRepository.getBalance(accountNumber);
        logger.info("Current balance for account {}: {}", accountNumber, balance);

        if (amount > balance) {
            logger.warn("Insufficient balance for account number: {}. Requested: {}, Available: {}", accountNumber,
                    amount, balance);

            responseObserver.onCompleted();

            return;
        }

        for (int i = 0; i < amount / 10; i++) {
            var money = Money.newBuilder()
                    .setAmount(10)
                    .build();

            logger.info("Dispensing $10 for account number: {}", accountNumber);

            accountRepository.deductBalance(accountNumber, 10);

            responseObserver.onNext(money);

            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> deposit(StreamObserver<AccountBalance> responseObserver) {
        return new StreamObserver<DepositRequest>() {

            private final StreamObserver<AccountBalance> respObserver = responseObserver;
            private int accountNumber;

            @Override
            public void onNext(DepositRequest request) {

                switch (request.getRequestCase()) {
                    case ACCOUNT_NUMBER -> {
                        logger.info("Setting account number for deposit: {}", request.getAccountNumber());
                        this.accountNumber = request.getAccountNumber();
                    }
                    case MONEY -> {
                        logger.info("Depositing amount: {} to account number: {}", request.getMoney().getAmount(),
                                accountNumber);
                        BankService.this.accountRepository.addAmount(accountNumber, request.getMoney().getAmount());
                    }
                    default -> {
                        logger.error("Received unknown request type in deposit for account number: {}", accountNumber);
                        throw new IllegalArgumentException("Unexpected value: " + request.getRequestCase());
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error during deposit for account number: {}", accountNumber, t.getMessage());
            }

            @Override
            public void onCompleted() {
                var balance = AccountBalance.newBuilder()
                        .setAccountNumber(accountNumber)
                        .setBalance(BankService.this.accountRepository.getBalance(accountNumber))
                        .build();

                logger.info("Deposit completed for account number: {}. New balance: {}", accountNumber,
                        balance.getBalance());

                respObserver.onNext(balance);
                respObserver.onCompleted();
            }
        };
    }

}
