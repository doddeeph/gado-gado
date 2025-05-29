package com.example.acid;

import java.math.BigDecimal;

public class BankACID {
    public static void main(String[] args) throws InterruptedException {
        BankDB db = new BankDB();
        BankTransaction transaction = new BankTransaction(db);

        transaction.transfer("Alice", "Bob", BigDecimal.valueOf(100));

        /* -------------- */

        System.out.println();
        transaction.transfer("Alice", "Bob", BigDecimal.valueOf(600)); // Insufficient funds

        /* -------------- */

        System.out.println();

        // Two concurrent transfers
        Thread t1 = new Thread(() -> transaction.transfer("Thread-1", "Alice", "Bob", BigDecimal.valueOf(100)));
        Thread t2 = new Thread(() -> transaction.transfer("Thread-2", "Bob", "Alice", BigDecimal.valueOf(200)));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}