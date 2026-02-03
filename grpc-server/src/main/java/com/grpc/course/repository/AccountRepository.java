package com.grpc.course.repository;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

    private final Map<Integer, Integer> accountData = new HashMap<>(Map.of(
            1, 100,
            2, 200,
            3, 300,
            4, 400,
            5, 500
    ));

    public Integer getBalance(int accountNumber) {
        return accountData.getOrDefault(accountNumber, 0);
    }

    public Map<Integer, Integer> getAllAccounts() {
        return Collections.unmodifiableMap(accountData);
    }

    public void deductBalance(int accountNumber, int amount) {
        accountData.computeIfPresent(accountNumber, (key, balance) -> balance - amount);
    }


    public void addAmount(int accountNumber, int amount) {
        accountData.computeIfPresent(accountNumber, (key, balance) -> balance + amount);
    }

}
