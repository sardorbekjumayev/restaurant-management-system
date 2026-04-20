package com.example.restaurantmanagementsystem.Model;

import java.time.LocalDateTime;

public class Receptionist extends Employee {

    public boolean createReservation(LocalDateTime time, int peopleCount, String notes, Customer customer) {
        Reservation reservation = new Reservation(time, peopleCount, notes, customer);
        return true;
    }

    public Receptionist(String name, String email, String phone) {
        super(name, email, phone);
    }
    public String getName(){return username;}
    public String getEmail(){return useremail;}
    public String getPhone(){return userphone;}
}