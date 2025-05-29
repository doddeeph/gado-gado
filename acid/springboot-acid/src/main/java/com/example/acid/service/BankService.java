package com.example.acid.service;

import com.example.acid.entity.Account;
import com.example.acid.exception.TransferException;
import com.example.acid.repository.AccountRepository;
import com.example.acid.service.dto.TransferDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class BankService {

    private final AccountRepository accountRepository;

    @Autowired
    public BankService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Add isolation level (REPEATABLE_READ or SERIALIZABLE for stricter control)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String transfer(TransferDto request) {
        log.info("{} started", Thread.currentThread().getName());

        Account fromAccount = accountRepository.findByName(request.getFrom()).orElseThrow();
        Account toAccount = accountRepository.findByName(request.getFrom()).orElseThrow();

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new TransferException("Insufficient funds");
        }

        // Simulate processing delay (to test concurrent access)
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(fromAccount);

        // Simulate a failure here to test rollback
        // if (true) throw new RuntimeException("Simulated error");

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(toAccount);

        log.info("{} committed", Thread.currentThread().getName());
        return "Transfer successful";
    }
}
