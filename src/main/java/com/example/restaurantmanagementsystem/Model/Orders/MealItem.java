package com.example.restaurantmanagementsystem.Model.Orders;

import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;

public class MealItem {
    private int mealItemID;
    private int orderId;
    private int quantity;
    private MenuItem menuItem;
    private int seatNumber;

    public MealItem(int mealItemID, int orderId, int quantity, MenuItem menuItem, int seatNumber) {
        this.mealItemID = mealItemID;
        this.orderId = orderId;
        this.quantity = quantity;
        this.menuItem = menuItem;
        this.seatNumber = seatNumber;
    }

    public int getMealItemID() {
        return mealItemID;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
}
