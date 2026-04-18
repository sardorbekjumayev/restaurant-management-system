package com.example.restaurantmanagementsystem.Model;

public class Employee extends Person{

    private int employeeID;
    private String dateJoined;

    private Account account;


    Employee(String name, String email, String phone,int employeeID, String dateJoined) {
        super(name, email, phone);

        this.name = name;
        this.email = email;
        this.phone = phone;
        this.employeeID = employeeID;
        this.dateJoined = dateJoined;

    }


    public int getEmployeeID() {
        return employeeID;
    }

    public String getDateJoined() {
        return dateJoined;
    }


}
