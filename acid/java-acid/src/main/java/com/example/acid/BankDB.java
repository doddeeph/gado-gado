package com.example.acid;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BankDB {

    private final Map<String, BigDecimal> accounts;

    public BankDB() {
        this.accounts = new HashMap<>();
        this.accounts.put("Alice", BigDecimal.valueOf(500));
        this.accounts.put("Bob", BigDecimal.valueOf(300));
    }

    public BigDecimal getBalance(String account) {
        return accounts.getOrDefault(account, BigDecimal.ZERO);
    }

    public void updateBalance(String account, BigDecimal amount) {
        accounts.put(account, amount);
    }

    public void showBalances() {
        accounts.forEach((account, balance) -> System.out.printf("%s: $%.0f%n", account, balance));
    }

    public Map<String, BigDecimal> getAccounts() {
        return accounts;
    }
}
