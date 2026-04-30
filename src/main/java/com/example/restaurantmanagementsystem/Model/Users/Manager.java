package com.example.restaurantmanagementsystem.Model.Users;

public class Manager extends Employee {
    public Manager(String name, String email, String phone, int employeeID, String dateJoined) {
        super(name, email, phone, employeeID, dateJoined, "Manager", null, 1);
    }
}
