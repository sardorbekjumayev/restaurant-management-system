package com.example.restaurantmanagementsystem.Model.Users;

import com.example.restaurantmanagementsystem.Enums.ReservationStatus;

import java.time.LocalDateTime;

public class Reservation {
    private int reservationId;
    private LocalDateTime timeOfReservation;
    private int peopleCount;
    private ReservationStatus status;
    private String notes;
    private LocalDateTime checkInTime;
    private Customer customer;
    private Integer tableId;
    private Integer branchId;

    public Reservation(int reservationId, LocalDateTime timeOfReservation, int peopleCount,
                       String notes, Customer customer, Integer tableId) {
        this(reservationId, timeOfReservation, peopleCount, notes, customer, tableId, customer == null ? null : customer.getBranchId());
    }

    public Reservation(int reservationId, LocalDateTime timeOfReservation, int peopleCount,
                       String notes, Customer customer, Integer tableId, Integer branchId) {
        this.reservationId = reservationId;
        this.timeOfReservation = timeOfReservation;
        this.peopleCount = peopleCount;
        this.notes = notes;
        this.customer = customer;
        this.tableId = tableId;
        this.branchId = branchId;
        this.status = ReservationStatus.requested;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public LocalDateTime getTimeOfReservation() {
        return timeOfReservation;
    }

    public void setTimeOfReservation(LocalDateTime timeOfReservation) {
        this.timeOfReservation = timeOfReservation;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }
}
