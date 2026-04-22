package com.example.restaurantmanagementsystem.Model.Payments;

public class CashTransaction extends Payment {

    public CashTransaction(double amount) {
        super(amount);
    }

    @Override
    public boolean processPayment() {
        // Cash is always accepted
        status = PaymentStatus.COMPLETED;
        return true;
    }
}
