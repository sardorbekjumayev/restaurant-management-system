package com.example.restaurantmanagementsystem.Model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String displayName;
    private Integer branchId;
    private String branchName;

    public User(String username, String password, String role) {
        this(0, username, password, role, username, null, null);
    }

    public User(int id, String username, String password, String role, String displayName,
                Integer branchId, String branchName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
        this.branchId = branchId;
        this.branchName = branchName;
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

    public String getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName == null || displayName.isBlank() ? username : displayName;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public boolean isManager() {
        return "Manager".equalsIgnoreCase(role);
    }
}
