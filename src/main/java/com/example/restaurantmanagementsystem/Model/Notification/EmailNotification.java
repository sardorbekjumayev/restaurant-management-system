package com.example.restaurantmanagementsystem.Model.Notification;

import java.time.LocalDate;

public class EmailNotification extends Notification {
    private String email;

    public EmailNotification(int notificationId, LocalDate createdOn, String content, String email) {
        super(notificationId, createdOn, content);
        this.email = email;
    }

    @Override
    public boolean send() {
        System.out.println("Sending email notification to: " + email);
        System.out.println("Content: " + getContent());
        return true;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}