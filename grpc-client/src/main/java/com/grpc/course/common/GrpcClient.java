package com.grpc.course.common;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grpc.course.AccountBalance;
import com.grpc.course.BalanceCheckRequest;
import com.grpc.course.BankServiceGrpc;
import com.grpc.course.DepositRequest;
import com.grpc.course.Money;
import com.grpc.course.TransferServiceGrpc;
import com.grpc.course.WithdrawRequest;
import com.grpc.course.TransferRequest;
import com.grpc.course.TransferResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

    private ManagedChannel channel;

    public GrpcClient(String host, int port) {
        initClient(host, port);
    }

    private void initClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    public void processBalance(int accountNumber) {
        var stub = BankServiceGrpc.newBlockingStub(channel);

        var balanceRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        var accountBalance = stub.getAccountBalance(balanceRequest);

        logger.info("Balance Response - Account Number: {}, Balance: {}",
                accountBalance.getAccountNumber(),
                accountBalance.getBalance());
    }

    public void asyncProcessBalance(int accountNumber) {
        var stub = BankServiceGrpc.newStub(channel);

        var balanceRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        stub.getAccountBalance(balanceRequest, createBalanceObserver());
    }

    public Iterator<Money> withdraw(int accountNumber, int amount) {
        var stub = BankServiceGrpc.newBlockingStub(channel);

        var request = WithdrawRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .setAmount(amount)
                .build();

        return stub.withdraw(request);
    }

    public void deposit(int accountNumber, int... amounts) {
        var stub = BankServiceGrpc.newStub(channel);

        var responseObserver = new StreamObserver<AccountBalance>() {
            @Override
            public void onNext(AccountBalance value) {
                logger.info("Final Account Balance - Account Number: {}, Balance: {}",
                        value.getAccountNumber(),
                        value.getBalance());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error during deposit", t);
            }

            @Override
            public void onCompleted() {
                logger.info("Deposit completed successfully");
            }
        };

        var requestObserver = stub.deposit(responseObserver);

        try {
            requestObserver.onNext(
                    DepositRequest
                            .newBuilder()
                            .setAccountNumber(accountNumber)
                            .build());

            for (int amount : amounts) {
                logger.info("Depositing {} to account {}", amount, accountNumber);

                var money = Money.newBuilder().setAmount(amount).build();

                requestObserver.onNext(
                        DepositRequest
                                .newBuilder()
                                .setMoney(money)
                                .build());

            }

            requestObserver.onCompleted();
            requestObserver.wait();
        } catch (Exception e) {
            logger.error("Error sending deposit requests", e);
            requestObserver.onError(e);
        }
    }


    public void transfer(int[][] transfers) {
        var stub = TransferServiceGrpc.newStub(channel);

        var responseObserver = new StreamObserver<TransferResponse>() {
            @Override
            public void onNext(TransferResponse value) {
                logger.info("Transfer Response - Status: {}, From Account: {}, To Account: {}",
                        value.getStatus(),
                        value.getFromAccount().getAccountNumber(),
                        value.getToAccount().getAccountNumber());
                logger.info("  From Balance: {}, To Balance: {}",
                        value.getFromAccount().getBalance(),
                        value.getToAccount().getBalance());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error during transfer", t);
            }

            @Override
            public void onCompleted() {
                logger.info("All transfers completed successfully");
            }
        };

        var requestObserver = stub.transfer(responseObserver);

        try {
            for (int[] transfer : transfers) {
                int fromAccount = transfer[0];
                int toAccount = transfer[1];
                int amount = transfer[2];

                logger.info("Transferring {} from account {} to account {}",
                        amount, fromAccount, toAccount);

                var transferRequest = TransferRequest.newBuilder()
                        .setFromAccount(fromAccount)
                        .setToAccount(toAccount)
                        .setAmount(amount)
                        .build();

                requestObserver.onNext(transferRequest);
            }

            requestObserver.onCompleted();
            requestObserver.wait();
        } catch (Exception e) {
            logger.error("Error sending transfer requests", e);
            requestObserver.onError(e);
        }
    }

    private StreamObserver<AccountBalance> createBalanceObserver() {
        return new StreamObserver<AccountBalance>() {
            @Override
            public void onNext(AccountBalance value) {
                logger.info("Balance Response - Account Number: {}, Balance: {}",
                        value.getAccountNumber(),
                        value.getBalance());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error fetching balance", t);
            }

            @Override
            public void onCompleted() {
                logger.info("Balance request completed");
            }
        };
    }

}
