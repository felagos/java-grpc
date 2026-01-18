package com.grpc.course.validator;

import java.util.Optional;

import io.grpc.Status;

public class RequestValidator {

    public static Optional<Status> validateAccount(int accountNumber) {
        if (accountNumber <= 0) {
            return Optional.of(Status.INVALID_ARGUMENT.withDescription("Account number must be positive"));
        }
        return Optional.empty();
    }

    public static Optional<Status> isAmountDivisibleBy10(int amount) {
        if (amount <= 0 && amount % 10 != 0) {
            return Optional.of(Status.INVALID_ARGUMENT.withDescription("Amount must be positive and divisible by 10"));
        }
        return Optional.empty();
    }

    public static Optional<Status> hasSufficientBalance(int amount, int balance) {
        if (amount > balance) {
            return Optional.of(Status.FAILED_PRECONDITION.withDescription("Insufficient balance"));
        }
        return Optional.empty();
    }


}
