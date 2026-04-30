package com.example.restaurantmanagementsystem.Model.Users;

public class Chef extends Employee {
    public Chef(String name, String email, String phone, int employeeID, String dateJoined) {
        super(name, email, phone, employeeID, dateJoined, "Chef", null, 1);
    }
}
