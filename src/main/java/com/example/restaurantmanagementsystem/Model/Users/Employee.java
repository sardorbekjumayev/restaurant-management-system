package com.example.restaurantmanagementsystem.Model.Users;

public class Employee extends Person {
    private int employeeID;
    private String dateJoined;
    private Account account;
    private String role;
    private int branchId;

    public Employee() {
    }

    public Employee(String fullName, String email, String phone, int employeeID, String dateJoined,
                    String role, Account account, int branchId) {
        super(fullName, email, phone);
        this.employeeID = employeeID;
        this.dateJoined = dateJoined;
        this.account = account;
        this.role = role;
        this.branchId = branchId;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public String getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }
}
