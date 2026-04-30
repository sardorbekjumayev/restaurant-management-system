package com.example.restaurantmanagementsystem.Model.Payments;

import com.example.restaurantmanagementsystem.Enums.PaymentMethod;
import com.example.restaurantmanagementsystem.Enums.PaymentStatus;

import java.time.LocalDateTime;

public class PaymentRecord {
    private int id;
    private int orderId;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private String details;

    public PaymentRecord(int id, int orderId, double amount, PaymentMethod method,
                         PaymentStatus status, LocalDateTime createdAt, String details) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDetails() {
        return details;
    }
}
