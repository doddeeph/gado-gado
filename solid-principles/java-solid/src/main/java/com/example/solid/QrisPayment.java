package com.example.solid;

public class QrisPayment implements PaymentStrategy, QRScanable {

    private String qrisCode;

    public QrisPayment(String qrisCode) {
        this.qrisCode = qrisCode;
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using QRIS.");
    }

    @Override
    public void scanQRCode() {

    }
}
