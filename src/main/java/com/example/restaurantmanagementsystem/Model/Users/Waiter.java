package com.example.restaurantmanagementsystem.Model.Users;

public class Waiter extends Employee {
    public Waiter(String name, String email, String phone, int employeeID, String dateJoined) {
        super(name, email, phone, employeeID, dateJoined, "Waiter", null, 1);
    }
}
