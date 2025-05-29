package com.example.solid;

public class GopayPayment implements PaymentStrategy, Refunable, UserAuthentication {

    private String gopayId;

    public GopayPayment(String gopayId) {
        this.gopayId = gopayId;
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Gopay.");
    }

    @Override
    public void refund(double amount) {

    }

    @Override
    public void authenticateUser() {

    }
}
