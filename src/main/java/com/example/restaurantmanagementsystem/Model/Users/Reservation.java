package com.example.restaurantmanagementsystem.Model.Users;

import com.example.restaurantmanagementsystem.Enums.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

    private int reservationId;
    private LocalDateTime timeOfReservation;
    private int peopleCount;
    private ReservationStatus status;
    private String notes;
    private LocalDate checkInTime;
    private Customer customer;

    ///Only receptionist can create object thru this:
    Reservation(LocalDateTime timeOfReservation, int peopleCount, String notes, Customer customer) {
        this.timeOfReservation = timeOfReservation;
        this.peopleCount = peopleCount;
        this.notes = notes;
        this.customer = customer;
        this.status = ReservationStatus.requested;
    }

    public boolean updatePeopleCount(int num) {
        try {
            peopleCount = peopleCount + num;
            return true;
        } catch (Exception e) {
            System.out.println("Please enter a valid number");
            return false;
        }
    }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public LocalDateTime getTimeOfReservation() { return timeOfReservation; }
    public void setTimeOfReservation(LocalDateTime timeOfReservation) { this.timeOfReservation = timeOfReservation; }

    public int getPeopleCount() { return peopleCount; }
    public void setPeopleCount(int peopleCount) { this.peopleCount = peopleCount; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDate checkInTime) { this.checkInTime = checkInTime; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}