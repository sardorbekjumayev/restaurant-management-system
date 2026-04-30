package com.example.restaurantmanagementsystem.Model.Users;

public class Account {
    private int id;
    private String username;
    private String password;
    private AccountStatus status;
    private String role;

    public Account() {
    }

    public Account(int id, String username, String password, AccountStatus status, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }
}
