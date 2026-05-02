package com.example.restaurantmanagementsystem.Model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class MenuSection {
    private int menuSectionID;
    private int menuId;
    private String title;
    private String description;
    private final List<MenuItem> menuItems = new ArrayList<>();

    public MenuSection(int menuSectionID, String title, String description) {
        this(menuSectionID, 0, title, description);
    }

    public MenuSection(int menuSectionID, int menuId, String title, String description) {
        this.menuSectionID = menuSectionID;
        this.menuId = menuId;
        this.title = title;
        this.description = description;
    }

    public boolean addMenuItem(MenuItem menuItem) {
        return menuItems.add(menuItem);
    }

    public int getMenuSectionID() {
        return menuSectionID;
    }

    public int getMenuId() {
        return menuId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    @Override
    public String toString() {
        return title;
    }
}
