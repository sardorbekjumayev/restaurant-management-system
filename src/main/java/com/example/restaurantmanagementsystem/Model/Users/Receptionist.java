package com.example.restaurantmanagementsystem.Model.Users;

import java.time.LocalDateTime;

public class Receptionist extends Employee {
    public Receptionist(String name, String email, String phone, int employeeID, String dateJoined) {
        super(name, email, phone, employeeID, dateJoined, "Receptionist", null, 1);
    }

    public Reservation createReservation(LocalDateTime time, int peopleCount, String notes, Customer customer) {
        return new Reservation(0, time, peopleCount, notes, customer, null, customer.getBranchId());
    }
}
