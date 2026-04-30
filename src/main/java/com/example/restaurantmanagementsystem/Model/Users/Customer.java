package com.example.restaurantmanagementsystem.Model.Users;

public class Customer extends Person {
    private int customerId;
    private Integer branchId;

    public Customer() {
    }

    public Customer(int customerId, String fullName, String email, String phone) {
        this(customerId, fullName, email, phone, null);
    }

    public Customer(int customerId, String fullName, String email, String phone, Integer branchId) {
        super(fullName, email, phone);
        this.customerId = customerId;
        this.branchId = branchId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }
}
