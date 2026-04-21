package org;

import java.time.LocalDateTime;

public abstract class Payment {
    protected double amount;
    protected PaymentStatus status;
    protected LocalDateTime createdAt;

    public Payment(double amount) {
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public abstract boolean processPayment();

    public double getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}