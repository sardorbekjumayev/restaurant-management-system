package com.example.restaurantmanagementsystem.Model.Payments;

import com.example.restaurantmanagementsystem.Enums.PaymentStatus;

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
