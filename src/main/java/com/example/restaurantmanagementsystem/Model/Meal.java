package com.example.restaurantmanagementsystem.Model;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    public int mealID;
    public TableSeat tableSeat;
    public List<MealItem> mealItems;

    public Meal(int mealID, TableSeat tableSeat) {
        this.mealID = mealID;
        this.tableSeat = tableSeat;
        this.mealItems = new ArrayList<>();
    }

    public boolean addMealItem(MealItem mealItem) {
        return this.mealItems.add(mealItem);
    }
}