package com.example.solid;

/*
 * S: Single Responsibility Principle
 * This interface has only one responsibility: define a common contract for payment strategies.
 *
 * O: Open/Closed Principle
 * New payment methods can be added without modifying existing code.
 */
public interface PaymentStrategy {

    void pay(double amount);

    // Problem with a Fat Interface (ISP Violation)
//    void refund(double amount);
//    void validateCard();               // Only for credit card
//    void scanQRCode();                 // Only for QRIS
//    void authenticateUser();           // Only for GoPay
//    void getTransactionHistory();      // Maybe only some implement this
}
