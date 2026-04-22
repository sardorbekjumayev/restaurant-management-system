package com.example.restaurantmanagementsystem.Model.Orders;

import com.example.restaurantmanagementsystem.Model.Restaurant.MenuItem;

public class MealItem {
    public int mealItemID;
    public int quantity;
    public MenuItem menuItem;

    public MealItem(int mealItemID, int quantity, MenuItem menuItem) {
        this.mealItemID = mealItemID;
        this.quantity = quantity;
        this.menuItem = menuItem;
    }

    public boolean updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
        return true;
    }
}