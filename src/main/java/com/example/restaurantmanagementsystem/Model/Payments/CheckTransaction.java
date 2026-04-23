package com.example.restaurantmanagementsystem.Model.Payments;

import com.example.restaurantmanagementsystem.Enums.PaymentStatus;

public class CheckTransaction extends Payment {

    private String checkNumber;

    public CheckTransaction(double amount, String checkNumber) {
        super(amount);
        this.checkNumber = checkNumber;
    }

    @Override
    public boolean processPayment() {
        if (checkNumber != null && !checkNumber.isEmpty()) {
            status = PaymentStatus.COMPLETED;
            return true;
        } else {
            status = PaymentStatus.FAILED;
            return false;
        }
    }
}
