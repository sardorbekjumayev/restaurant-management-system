package com.example.restaurantmanagementsystem.Controller;

public final class DashboardViewResolver {
    private DashboardViewResolver() {
    }

    public static String resolveFxml(String role) {
        return switch (role) {
            case "Admin" -> "/com/example/restaurantmanagementsystem/AdminDashboard.fxml";
            case "Manager" -> "/com/example/restaurantmanagementsystem/ManagerDashboard.fxml";
            case "Waiter" -> "/com/example/restaurantmanagementsystem/WaiterDashboard.fxml";
            case "Receptionist" -> "/com/example/restaurantmanagementsystem/ReceptionistDashboard.fxml";
            case "Chef" -> "/com/example/restaurantmanagementsystem/ChefDashboard.fxml";
            case "Cashier" -> "/com/example/restaurantmanagementsystem/CashierDashboard.fxml";
            default -> "/com/example/restaurantmanagementsystem/ManagerDashboard.fxml";
        };
    }
}
