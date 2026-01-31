package com.grpc.course.common;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

public class GrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

    private ManagedChannel channel;
    private ManagedChannel channelAlive;

    public GrpcClient(String host, int port) {
        initClient(host, port);
    }

    private void initClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        channelAlive = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
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

    public void processBalanceAlive(int accountNumber) {
        var stub = BankServiceGrpc.newBlockingStub(channelAlive);

        var balanceRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        var accountBalance = stub.getAccountBalance(balanceRequest);

        logger.info("Balance Response (Keep-Alive) - Account Number: {}, Balance: {}",
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
        CountDownLatch latch = new CountDownLatch(1);

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
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("Deposit completed successfully");
                latch.countDown();
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
            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error sending deposit requests", e);
            requestObserver.onError(e);
        }
    }


    public Iterator<TransferResponse> transfer(int[][] transfers) {
        var stub = TransferServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        List<TransferResponse> responses = new ArrayList<>();

        var responseObserver = new StreamObserver<TransferResponse>() {
            @Override
            public void onNext(TransferResponse value) {
                responses.add(value);
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
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("All transfers completed successfully");
                latch.countDown();
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
            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error sending transfer requests", e);
            requestObserver.onError(e);
        }

        return responses.iterator();
    }

    public void processWithdraw(int accountNumber, int amount) {
        try {
            logger.info("Processing withdraw for account {} with amount {}", accountNumber, amount);
            var moneyIterator = withdraw(accountNumber, amount);
            
            while (moneyIterator.hasNext()) {
                var money = moneyIterator.next();
                logger.info("Received money: ${}", money.getAmount());
            }
            
            logger.info("Withdraw completed successfully");
        } catch (Exception e) {
            logger.error("Error during withdraw: {}", e.getMessage());
        }
    }

    public void processWithdrawWithJwt(int accountNumber, int amount, String jwtToken) {
        try {
            logger.info("Processing withdraw with JWT for account {} with amount {}", accountNumber, amount);
            
            var stub = BankServiceGrpc.newBlockingStub(channel);
            
            // Crear metadata con JWT token
            Metadata metadata = new Metadata();
            Metadata.Key<String> jwtKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(jwtKey, jwtToken);
            
            // Aplicar metadata al stub
            var stubWithAuth = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
            
            var request = WithdrawRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .setAmount(amount)
                    .build();
            
            var moneyIterator = stubWithAuth.withdraw(request);
            
            while (moneyIterator.hasNext()) {
                var money = moneyIterator.next();
                logger.info("Received money: ${}", money.getAmount());
            }
            
            logger.info("JWT-authenticated withdraw completed successfully");
        } catch (Exception e) {
            logger.error("Error during JWT withdraw: {}", e.getMessage());
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
