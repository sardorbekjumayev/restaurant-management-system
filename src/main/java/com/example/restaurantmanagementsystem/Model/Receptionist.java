package com.example.restaurantmanagementsystem.Model;

import java.time.LocalDateTime;

public class Receptionist extends Employee {
    Receptionist(String name, String email, String phone, int employeeID, String dateJoined) {
        super(name, email, phone, employeeID, dateJoined);
    }

    public boolean createReservation(LocalDateTime time, int peopleCount, String notes, Customer customer) {
        Reservation reservation = new Reservation(time, peopleCount, notes, customer);
        return true;
    }
}
