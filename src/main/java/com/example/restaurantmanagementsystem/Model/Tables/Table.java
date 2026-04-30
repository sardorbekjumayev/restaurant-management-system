package com.example.restaurantmanagementsystem.Model.Tables;

import com.example.restaurantmanagementsystem.Enums.TableStatus;

public class Table {
  private int tableId;
  private int branchId;
  private String tableNumber;
  private TableStatus status;
  private int maxCapacity;
  private int locationId;

  public Table(int tableId, TableStatus status, int maxCapacity, int locationId) {
    this(tableId, 0, "T" + tableId, status, maxCapacity, locationId);
  }

  public Table(int tableId, int branchId, String tableNumber, TableStatus status, int maxCapacity, int locationId) {
    this.tableId = tableId;
    this.branchId = branchId;
    this.tableNumber = tableNumber;
    this.status = status;
    this.maxCapacity = maxCapacity;
    this.locationId = locationId;
  }

  public int getTableId() {
    return tableId;
  }

  public int getBranchId() {
    return branchId;
  }

  public String getTableNumber() {
    return tableNumber;
  }

  public TableStatus getStatus() {
    return status;
  }

  public void setStatus(TableStatus status) {
    this.status = status;
  }

  public int getMaxCapacity() {
    return maxCapacity;
  }

  public int getLocationId() {
    return locationId;
  }
}
