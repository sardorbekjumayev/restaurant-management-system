package com.example.restaurantmanagementsystem.Model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private int id;
    private String name;
    private final List<Branch> branches = new ArrayList<>();

    public Restaurant(String name) {
        this(0, name);
    }

    public Restaurant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean addBranch(Branch branch) {
        return branches.add(branch);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Branch> getBranches() {
        return branches;
    }
}
