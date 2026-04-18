package com.example.restaurantmanagementsystem.Model;

public class Account {

    private int id;
    private String password;
    private String Address;
    private String Status;

    Account(int id, String password, String Address, String Status){

        this.id = id;
        this.password = password;
        this.Address = Address;
        this.Status = Status;

    }

}
