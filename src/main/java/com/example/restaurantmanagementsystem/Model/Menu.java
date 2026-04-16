package com.example.restaurantmanagementsystem.Model;
import java.util.ArrayList;
import java.util.List;

public class Menu {
    private int menuID;
    private String title;
    private String description;
    private List<MenuSection> menuSections;

    public Menu(int menuID, String title, String description) {
        this.menuID = menuID;
        this.title = title;
        this.description = description;
        this.menuSections = new ArrayList<>();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MenuSection> getMenuSections() {
        return menuSections;
    }
}
