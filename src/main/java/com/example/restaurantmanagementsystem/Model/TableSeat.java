package com.example.restaurantmanagementsystem.Model;

import com.example.restaurantmanagementsystem.Enums.SeatType;

public class TableSeat {
    private final int tableSeatNumber;
    private SeatType type;

    public TableSeat(int tableSeatNumber,SeatType type){
        if (tableSeatNumber <= 0) {
            throw new IllegalArgumentException("Seat number must be positive");
        }
        if (type == null) {
            throw new IllegalArgumentException("Seat type cannot be null");
        }
        this.tableSeatNumber=tableSeatNumber;
        this.type=type;
    }

    public boolean updateSeatType(SeatType newType) {
            if (newType == null) {
                throw new IllegalArgumentException("Seat type cannot be null");
            }

            if (this.type == newType) {
                return false; // no change
            }

            this.type = newType;
            return true;
        }
    public int getTableSeatNumber() { return tableSeatNumber; }
    public SeatType getType() { return type; }

    @Override
    public String toString() {
        return "TableSeat{" +
                "tableSeatNumber=" + tableSeatNumber +
                ", type=" + type +
                '}';
    }
}
