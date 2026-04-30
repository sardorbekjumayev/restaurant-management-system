package com.example.restaurantmanagementsystem.Model.Orders;

import com.example.restaurantmanagementsystem.Enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderID;
    private Integer branchId;
    private Integer customerId;
    private Integer waiterId;
    private Integer tableId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private double totalAmount;
    private final List<MealItem> items = new ArrayList<>();

    public Order(int orderID, Integer customerId, Integer waiterId, Integer tableId,
                 OrderStatus status, LocalDateTime createdAt, double totalAmount) {
        this(orderID, null, customerId, waiterId, tableId, status, createdAt, totalAmount);
    }

    public Order(int orderID, Integer branchId, Integer customerId, Integer waiterId, Integer tableId,
                 OrderStatus status, LocalDateTime createdAt, double totalAmount) {
        this.orderID = orderID;
        this.branchId = branchId;
        this.customerId = customerId;
        this.waiterId = waiterId;
        this.tableId = tableId;
        this.status = status;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
    }

    public int getOrderID() {
        return orderID;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public Integer getWaiterId() {
        return waiterId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<MealItem> getItems() {
        return items;
    }
}
