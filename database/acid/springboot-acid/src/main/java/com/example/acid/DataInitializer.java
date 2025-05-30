package com.example.acid;

import com.example.acid.entity.Account;
import com.example.acid.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Autowired
    public DataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Account> initAccounts = Arrays.asList(
                Account.builder().name("Alice").balance(BigDecimal.valueOf(500)).build(),
                Account.builder().name("Bob").balance(BigDecimal.valueOf(300)).build());
        accountRepository.saveAll(initAccounts);
        System.out.println("Accounts initialized.");
    }
}
