package com.example.restaurantmanagementsystem.Model.Restaurant;

import com.example.restaurantmanagementsystem.Model.Tables.TableChart;
import com.example.restaurantmanagementsystem.Model.Users.Address;

public class Branch {
    private int id;
    private String name;
    private Address location;
    private TableChart tableChart;
    private Menu menu;
    private String managerName;
    private String managerUsername;

    public Branch(String name, Address location) {
        this(0, name, location);
    }

    public Branch(int id, String name, Address location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public boolean addTableChart(TableChart tableChart) {
        this.tableChart = tableChart;
        return true;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getLocation() {
        return location;
    }

    public TableChart getTableChart() {
        return tableChart;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerUsername() {
        return managerUsername;
    }

    public void setManagerUsername(String managerUsername) {
        this.managerUsername = managerUsername;
    }

    public String getAddressSummary() {
        if (location == null) {
            return "";
        }
        return String.join(", ",
                safe(location.getStreet()),
                safe(location.getDistrict()),
                safe(location.getCity()),
                safe(location.getCountry()));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
