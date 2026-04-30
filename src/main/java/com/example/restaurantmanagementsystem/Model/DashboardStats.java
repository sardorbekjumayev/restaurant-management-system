package com.example.restaurantmanagementsystem.Model;

public class DashboardStats {
    private final int employeeCount;
    private final int customerCount;
    private final int menuItemCount;
    private final int reservationCount;
    private final int activeOrderCount;

    public DashboardStats(int employeeCount, int customerCount, int menuItemCount,
                          int reservationCount, int activeOrderCount) {
        this.employeeCount = employeeCount;
        this.customerCount = customerCount;
        this.menuItemCount = menuItemCount;
        this.reservationCount = reservationCount;
        this.activeOrderCount = activeOrderCount;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public int getMenuItemCount() {
        return menuItemCount;
    }

    public int getReservationCount() {
        return reservationCount;
    }

    public int getActiveOrderCount() {
        return activeOrderCount;
    }
}
