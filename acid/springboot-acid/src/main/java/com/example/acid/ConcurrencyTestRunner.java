package com.example.acid;

import com.example.acid.service.BankService;
import com.example.acid.service.dto.TransferDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Log4j2
@Component
public class ConcurrencyTestRunner implements CommandLineRunner {

    private final BankService bankService;

    @Autowired
    public ConcurrencyTestRunner(BankService bankService) {
        this.bankService = bankService;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread thread1 = new Thread(() -> {
            try {
                bankService.transfer(TransferDto.builder()
                        .from("Alice").to("Bob").amount(BigDecimal.valueOf(100))
                        .build());
            } catch (Exception e) {
                logError(e);
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                bankService.transfer(TransferDto.builder()
                        .from("Alice").to("Bob").amount(BigDecimal.valueOf(600))
                        .build());
            } catch (Exception e) {
                logError(e);
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }

    private void logError(Exception e) {
        log.error("{} failed -> {}", Thread.currentThread().getName(), e.getMessage());
    }
}
