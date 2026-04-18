package com.example.restaurantmanagementsystem.Model;

import com.example.restaurantmanagementsystem.Enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class Order {
    public int orderID;
    public OrderStatus status;
    public List<Meal> meals;

    public Order(int orderID) {
        this.orderID = orderID;
        this.status = OrderStatus.NONE;
        this.meals = new ArrayList<>();
    }

    public boolean addMeal(Meal meal) {
        return this.meals.add(meal);
    }

    public boolean removeMeal(Meal meal) {
        return this.meals.remove(meal);
    }

    public OrderStatus setStatus(OrderStatus newStatus) {
        this.status = newStatus;
        return this.status;
    }
}