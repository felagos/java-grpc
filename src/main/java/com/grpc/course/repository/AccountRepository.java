package com.grpc.course.repository;

import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

    private final Map<Integer, Integer> accountData = Map.of(
            1, 100,
            2, 200,
            3, 300,
            4, 400,
            5, 500
    );

    public Integer getBalance(int accountNumber) {
        return accountData.getOrDefault(accountNumber, 0);
    }

}
