package com.example.restaurantmanagementsystem.Model;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private String name;
    private List<Branch> branches;

    public Restaurant(String name) {
        this.name = name;
        this.branches = new ArrayList<>();
    }

    public boolean addBranch(Branch branch) {
        return branches.add(branch);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Branch> getBranches() {
        return branches;
    }
}
