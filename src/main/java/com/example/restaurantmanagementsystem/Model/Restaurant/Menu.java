package com.example.restaurantmanagementsystem.Model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private int menuID;
    private int branchId;
    private String title;
    private String description;
    private final List<MenuSection> menuSections = new ArrayList<>();

    public Menu(int menuID, String title, String description) {
        this(menuID, 0, title, description);
    }

    public Menu(int menuID, int branchId, String title, String description) {
        this.menuID = menuID;
        this.branchId = branchId;
        this.title = title;
        this.description = description;
    }

    public boolean addMenuSection(MenuSection menuSection) {
        return menuSections.add(menuSection);
    }

    public int getMenuID() {
        return menuID;
    }

    public void setMenuID(int menuID) {
        this.menuID = menuID;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<MenuSection> getMenuSections() {
        return menuSections;
    }
}
