package com.example.solid;

/*
 * Implements the PaymentStrategy interface.
 * Encapsulates Credit Card-specific logic.
 */
public class CreditCardPayment implements PaymentStrategy, Refunable, CardValidation, TransactionHistory {

    private String cardNumber;
    private String cardHolder;
    private String cvv;

    public CreditCardPayment(String cardNumber, String cardHolder, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.cvv = cvv;
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Credit Card.");
    }

    @Override
    public void validateCard() {

    }

    @Override
    public void refund(double amount) {

    }

    @Override
    public void getTransactionHistory() {

    }
}
