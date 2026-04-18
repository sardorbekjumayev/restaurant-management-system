package com.example.restaurantmanagementsystem.db;

public class CheckRole {


    private String rolee;

    public void userRole(String username, String password, String role) {



        if (password.equals("pass") && username.equals("manager")){
            this.rolee = role;
        } else if (password.equals("123") && username.equals("waiter")) {
            this.rolee = role;
        }
    }


    public String getRolee() {
        return rolee;
    }
}
