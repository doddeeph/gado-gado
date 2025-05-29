package com.example.acid;

import java.math.BigDecimal;

public class BankTransaction {

    private final BankDB bankDB;

    public BankTransaction(BankDB bankDB) {
        this.bankDB = bankDB;
    }

    public void transfer(String from, String to, BigDecimal amount) {
        System.out.println("Before transaction:");
        bankDB.showBalances();

        System.out.printf("Transfer $%.0f from %s to %s%n", amount, from, to);
        System.out.println(">>> Starting transaction");

        // Backup current state (for atomicity/rollback)
        BigDecimal balanceFrom = bankDB.getBalance(from);
        BigDecimal balanceTo = bankDB.getBalance(to);

        try {
            // Step 1: Check for sufficient balance (Consistency)
            if (balanceFrom.compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient funds in " + from + "'s account");
            }

            // Step 2: Deduct from sender (Atomicity step 1)
            bankDB.updateBalance(from, balanceFrom.subtract(amount));

            // Simulate a crash/failure (uncomment to test atomicity)
            // if (true) throw new RuntimeException("Unexpected error after deducting!");

            // Step 3: Add to receiver (Atomicity step 2)
            bankDB.updateBalance(to, balanceTo.add(amount));

            // Commit is done (Durability implied here)
            System.out.println(">>> Transaction committed");

        } catch (Exception e) {
            // Rollback (Atomicity)
            bankDB.updateBalance(from, balanceFrom);
            bankDB.updateBalance(to, balanceTo);

            System.out.printf(">>> Transaction failed. Rolled back. Reason: %s%n", e.getMessage());
        }

        System.out.println("After transaction:");
        bankDB.showBalances();
    }

    // Simulated transfer with Isolation
    public void transfer(String threadName, String from, String to, BigDecimal amount) {
        synchronized (bankDB.getAccounts()) {
            System.out.printf("[%s] Before transaction:%n", threadName);
            bankDB.showBalances();

            System.out.printf("[%s] Transfer $%.0f from %s to %s%n", threadName, amount, from, to);
            System.out.printf("[%s] >>> Starting transaction%n", threadName);

            // Backup current state (for atomicity/rollback)
            BigDecimal balanceFrom = bankDB.getBalance(from);
            BigDecimal balanceTo = bankDB.getBalance(to);

            try {
                // Step 1: Check for sufficient balance (Consistency)
                if (balanceFrom.compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient funds in " + from + "'s account");
                }

                // Step 2: Deduct from sender (Atomicity step 1)
                bankDB.updateBalance(from, balanceFrom.subtract(amount));

                // Simulate a crash/failure (uncomment to test atomicity)
                // if (true) throw new RuntimeException("Unexpected error after deducting!");

                // Step 3: Add to receiver (Atomicity step 2)
                bankDB.updateBalance(to, balanceTo.add(amount));

                // Commit is done (Durability implied here)
                System.out.printf("[%s] >>> Transaction committed%n", threadName);
            } catch (Exception e) {
                // Rollback (Atomicity)
                bankDB.updateBalance(from, balanceFrom);
                bankDB.updateBalance(to, balanceTo);

                System.out.printf("[%s] >>> Transaction failed. Rolled back. Reason: %s%n", threadName, e.getMessage());
            }

            System.out.printf("[%s] >>> After transaction:%n", threadName);
            bankDB.showBalances();
        }
    }
}
