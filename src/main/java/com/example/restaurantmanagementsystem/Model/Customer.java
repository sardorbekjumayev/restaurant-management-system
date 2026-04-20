package com.example.restaurantmanagementsystem.Model;

import java.time.LocalDateTime;

public class Customer extends Person {
    LocalDateTime lastVisited;

    public Customer(String name, String email, String phone) {
        super(name, email, phone);
    }

    public String getName(){return username;}
    public String getEmail(){return useremail;}
    public String getPhone(){return userphone;}
}
