package com.example.restaurantmanagementsystem.Model.Payments;

public class CreditCardTransaction extends Payment {

    private String cardNumber;
    private String cardHolderName;

    public CreditCardTransaction(double amount, String cardNumber, String cardHolderName) {
        super(amount);
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
    }

    @Override
    public boolean processPayment() {
        // Simulate validation
        if (cardNumber != null && cardNumber.length() >= 12) {
            status = PaymentStatus.COMPLETED;
            return true;
        } else {
            status = PaymentStatus.FAILED;
            return false;
        }
    }
}