package com.example.restaurantmanagementsystem.Model;

public class Branch {
    private String name;
    private Address location;
    private TableChart tableChart;
    private Menu menu;

    public Branch(String name, Address location) {
        this.name = name;
        this.location = location;
    }

    public boolean addTableChart(TableChart tableChart) {
        this.tableChart = tableChart;
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getLocation() {
        return location;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
