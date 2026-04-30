package com.example.restaurantmanagementsystem.Model.Restaurant;

public class MenuItem {
    private int menuItemID;
    private int sectionId;
    private String title;
    private String description;
    private double price;
    private boolean available;
    private String imageUrl; // New field for image

    public MenuItem(int menuItemID, String title, String description, double price) {
        this(menuItemID, 0, title, description, price, true, null);
    }

    public MenuItem(int menuItemID, int sectionId, String title, String description, double price, boolean available) {
        this(menuItemID, sectionId, title, description, price, available, null);
    }

    public MenuItem(int menuItemID, int sectionId, String title, String description, double price, boolean available, String imageUrl) {
        this.menuItemID = menuItemID;
        this.sectionId = sectionId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.available = available;
        this.imageUrl = imageUrl;
    }

    public int getMenuItemID() {
        return menuItemID;
    }

    public void setMenuItemID(int menuItemID) {
        this.menuItemID = menuItemID;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
