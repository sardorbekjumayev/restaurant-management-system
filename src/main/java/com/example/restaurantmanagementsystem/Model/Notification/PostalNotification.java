package com.example.restaurantmanagementsystem.Model.Notification;

import java.time.LocalDate;

public class PostalNotification extends Notification {
    private String phone;

    public PostalNotification(int notificationId, LocalDate createdOn, String content, String phone) {
        super(notificationId, createdOn, content);
        this.phone = phone;
    }

    @Override
    public boolean send() {
        System.out.println("Sending postal notification to: " + phone);
        System.out.println("Content: " + getContent());
        return true;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}