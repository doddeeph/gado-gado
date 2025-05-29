package com.example.acid.service;

import com.example.acid.entity.Account;
import com.example.acid.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializerService implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Autowired
    public DataInitializerService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Account> initAccounts = Arrays.asList(
                new Account("Alice", BigDecimal.valueOf(500)),
                new Account("Bob", BigDecimal.valueOf(300)));
        accountRepository.saveAll(initAccounts);
        System.out.println("Accounts initialized.");
    }
}
