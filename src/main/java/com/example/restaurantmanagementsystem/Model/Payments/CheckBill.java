package com.example.restaurantmanagementsystem.Model.Payments;

import java.util.ArrayList;
import java.util.List;

public class CheckBill {

    private List<String> items;
    private double totalAmount;
    private Payment payment;

    public CheckBill() {
        items = new ArrayList<>();
        totalAmount = 0;
    }

    public void addItem(String item, double price) {
        items.add(item);
        totalAmount += price;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public boolean pay(Payment payment) {
        this.payment = payment;

        if (payment.getAmount() < totalAmount) {
            System.out.println("Insufficient payment!");
            return false;
        }

        return payment.processPayment();
    }

    public void printBill() {
        System.out.println("---- BILL ----");
        for (String item : items) {
            System.out.println(item);
        }
        System.out.println("Total: " + totalAmount);
        System.out.println("Status: " + (payment != null ? payment.getStatus() : "UNPAID"));
    }
}
