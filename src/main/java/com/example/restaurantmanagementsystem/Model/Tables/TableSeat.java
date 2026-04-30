package com.example.restaurantmanagementsystem.Model.Tables;

import com.example.restaurantmanagementsystem.Enums.SeatType;

public class TableSeat {
    private int seatId;
    private int tableId;
    private String seatLabel;
    private SeatType seatType;

    public TableSeat(int seatId, int tableId, String seatLabel, SeatType seatType) {
        this.seatId = seatId;
        this.tableId = tableId;
        this.seatLabel = seatLabel;
        this.seatType = seatType;
    }

    public int getSeatId() {
        return seatId;
    }

    public int getTableId() {
        return tableId;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public SeatType getSeatType() {
        return seatType;
    }
}
