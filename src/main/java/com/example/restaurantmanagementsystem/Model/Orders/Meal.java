package com.example.restaurantmanagementsystem.Model.Orders;

import com.example.restaurantmanagementsystem.Model.Tables.TableSeat;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    private int mealID;
    private TableSeat tableSeat;
    private final List<MealItem> mealItems = new ArrayList<>();

    public Meal(int mealID, TableSeat tableSeat) {
        this.mealID = mealID;
        this.tableSeat = tableSeat;
    }

    public boolean addMealItem(MealItem mealItem) {
        return mealItems.add(mealItem);
    }

    public int getMealID() {
        return mealID;
    }

    public TableSeat getTableSeat() {
        return tableSeat;
    }

    public List<MealItem> getMealItems() {
        return mealItems;
    }
}
