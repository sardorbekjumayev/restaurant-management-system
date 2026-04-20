package com.example.restaurantmanagementsystem.Model.Notification;

import java.time.LocalDate;

public abstract class Notification {
    private int notificationId;
    private LocalDate createdOn;
    private String content;

    public Notification(int notificationId, LocalDate createdOn, String content) {
        this.notificationId = notificationId;
        this.createdOn = createdOn;
        this.content = content;
    }

    public abstract boolean send();

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    public LocalDate getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDate createdOn) { this.createdOn = createdOn; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}