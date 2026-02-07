package com.grpc.course.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grpc.course.BankServiceGrpc;
import com.grpc.course.GuessNumberGrpc;
import com.grpc.course.TransferServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.server.host}")
    private String grpcServerHost;

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    /**
     * Standard gRPC channel without keep-alive
     */
    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder
                .forAddress(grpcServerHost, grpcServerPort)
                .usePlaintext()
                .build();
    }

    /**
     * gRPC channel with keep-alive configuration
     */
    @Bean(name = "managedChannelAlive")
    public ManagedChannel managedChannelAlive() {
        return ManagedChannelBuilder
                .forAddress(grpcServerHost, grpcServerPort)
                .usePlaintext()
                .keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    /**
     * BankService blocking stub bean
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public BankServiceGrpc.BankServiceBlockingStub bankServiceBlockingStub(
            @Qualifier("managedChannel") ManagedChannel channel) {
        return BankServiceGrpc.newBlockingStub(channel);
    }

    /**
     * BankService async stub bean
     */
    @Bean
    public BankServiceGrpc.BankServiceStub bankServiceAsyncStub(
            @Qualifier("managedChannel") ManagedChannel channel) {
        return BankServiceGrpc.newStub(channel);
    }

    /**
     * BankService blocking stub with keep-alive bean
     */
    @Bean(name = "bankServiceBlockingStubAlive")
    public BankServiceGrpc.BankServiceBlockingStub bankServiceBlockingStubAlive(
            @Qualifier("managedChannelAlive") ManagedChannel channelAlive) {
        return BankServiceGrpc.newBlockingStub(channelAlive);
    }

    /**
     * TransferService blocking stub bean
     */
    @Bean
    public TransferServiceGrpc.TransferServiceBlockingStub transferServiceBlockingStub(
            @Qualifier("managedChannel") ManagedChannel channel) {
        return TransferServiceGrpc.newBlockingStub(channel);
    }

    /**
     * TransferService async stub bean
     */
    @Bean
    public TransferServiceGrpc.TransferServiceStub transferServiceAsyncStub(
            @Qualifier("managedChannel") ManagedChannel channel) {
        return TransferServiceGrpc.newStub(channel);
    }

    /**
     * GuessNumberGame service blocking stub bean
     */
    @Bean
    public GuessNumberGrpc.GuessNumberBlockingStub guessNumberBlockingStub(
            @Qualifier("managedChannel") ManagedChannel channel) {
        return GuessNumberGrpc.newBlockingStub(channel);
    }

    /**
     * GuessNumberGame service async stub bean
     */
    @Bean
    public GuessNumberGrpc.GuessNumberStub guessNumberAsyncStub(
            @Qualifier("managedChannel") ManagedChannel channel) {
        return GuessNumberGrpc.newStub(channel);
    }
}
