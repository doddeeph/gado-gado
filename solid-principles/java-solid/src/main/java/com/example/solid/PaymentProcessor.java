package com.example.solid;

/*
 * D: Dependency Inversion Principle
 * The context depends on the abstraction PaymentStrategy, not concrete classes.
 *
 * L: Liskov Substitution Principle
 * Any subclass of PaymentStrategy can replace another without affecting functionality.
 */
public class PaymentProcessor {

    private PaymentStrategy paymentStrategy;

    public PaymentProcessor(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void processPayment(double amount) {
        paymentStrategy.pay(amount);
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
}
