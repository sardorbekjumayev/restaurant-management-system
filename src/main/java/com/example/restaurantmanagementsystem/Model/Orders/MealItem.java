package com.example.restaurantmanagementsystem.Model.Orders;

import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;

public class MealItem {
    private int mealItemID;
    private int orderId;
    private int quantity;
    private MenuItem menuItem;

    public MealItem(int mealItemID, int orderId, int quantity, MenuItem menuItem) {
        this.mealItemID = mealItemID;
        this.orderId = orderId;
        this.quantity = quantity;
        this.menuItem = menuItem;
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
}
