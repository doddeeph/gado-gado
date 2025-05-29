package com.example.acid.service;

import com.example.acid.entity.Account;
import com.example.acid.exception.TransferException;
import com.example.acid.repository.AccountRepository;
import com.example.acid.service.dto.TransferDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankService {

    private final AccountRepository accountRepository;

    @Autowired
    public BankService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public String transfer(TransferDto request) {
        Account fromAccount = accountRepository.findByName(request.getFrom()).orElseThrow();
        Account toAccount = accountRepository.findByName(request.getFrom()).orElseThrow();

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new TransferException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(fromAccount);

        // Simulate a failure here to test rollback
        // if (true) throw new RuntimeException("Simulated error");

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(toAccount);

        return "Transfer successful";
    }
}
