package com.example.solid;

public class Main {

    /*
     * SOLID Principles:
     * 1. Single Responsibility
     * 2. Open/Closed
     * 3. Liskov Substitution
     * 4. Interface Segregation
     * 5. Dependency Inversion
     */
    public static void main(String[] args) {
        PaymentProcessor paymentProcessor = new PaymentProcessor(new CreditCardPayment("123456789", "John Doe", "123"));
        paymentProcessor.processPayment(100.0);

        paymentProcessor.setPaymentStrategy(new GopayPayment("gopay-123"));
        paymentProcessor.processPayment(50.0);

        paymentProcessor.setPaymentStrategy(new QrisPayment("qris-code-xyz"));
        paymentProcessor.processPayment(75.0);
    }
}