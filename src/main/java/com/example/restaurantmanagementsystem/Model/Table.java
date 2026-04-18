package com.example.restaurantmanagementsystem.Model;

import com.example.restaurantmanagementsystem.Enums.TableStatus;

public class Table {

  private final int tableId;
  private TableStatus status;
  private int maxCapacity;
  private int locationId;

  public Table(int tableId, TableStatus status, int maxCapacity, int locationId) {
    if (tableId <= 0) {
      throw new IllegalArgumentException("Table ID must be positive");
    }
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    if (maxCapacity <= 0) {
      throw new IllegalArgumentException("Max capacity must be positive");
    }
    if (locationId < 0) {
      throw new IllegalArgumentException("Location ID cannot be negative");
    }

    this.tableId = tableId;
    this.status = status;
    this.maxCapacity = maxCapacity;
    this.locationId = locationId;
  }

  public int getTableId() {
    return tableId;
  }

  public TableStatus getStatus() {
    return status;
  }

  public void setStatus(TableStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    this.status = status;
  }

  public int getMaxCapacity() {
    return maxCapacity;
  }

  public void setMaxCapacity(int maxCapacity) {
    if (maxCapacity <= 0) {
      throw new IllegalArgumentException("Max capacity must be positive");
    }
    this.maxCapacity = maxCapacity;
  }

  public int getLocationId() {
    return locationId;
  }

  public void setLocationId(int locationId) {
    if (locationId < 0) {
      throw new IllegalArgumentException("Location ID cannot be negative");
    }
    this.locationId = locationId;
  }
  public boolean isAvailable() {
    return status == TableStatus.FREE;
  }

  public boolean reserve() {
    if (!isAvailable()) return false;
    status = TableStatus.RESERVED;
    return true;
  }

  public boolean release() {
    if (status == TableStatus.RESERVED || status == TableStatus.OCCUPIED) {
      status = TableStatus.FREE;
      return true;
    }
    return false;
  }

  public String toString() {
    return "Table{" +
            "tableId=" + tableId +
            ", status=" + status +
            ", maxCapacity=" + maxCapacity +
            ", locationId=" + locationId +
            '}';
  }
}
