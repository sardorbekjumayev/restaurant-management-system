package com.example.restaurantmanagementsystem.Controller;

import com.example.restaurantmanagementsystem.Model.DashboardStats;
import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.service.ManagementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

// Shared dashboard base that keeps only the common user/session/status behavior.
public abstract class BaseDashboardController {
    protected ManagementService managementService;
    protected User currentUser;

    @FXML
    protected Label welcomeLabel;
    @FXML
    protected Label employeeOverviewTitleLabel;
    @FXML
    protected Label customerOverviewTitleLabel;
    @FXML
    protected Label menuOverviewTitleLabel;
    @FXML
    protected Label reservationOverviewTitleLabel;
    @FXML
    protected Label activeOrderOverviewTitleLabel;
    @FXML
    protected Label employeeCountLabel;
    @FXML
    protected Label customerCountLabel;
    @FXML
    protected Label menuCountLabel;
    @FXML
    protected Label reservationCountLabel;
    @FXML
    protected Label activeOrderCountLabel;
    @FXML
    protected Label statusLabel;
    @FXML
    protected Button logOutButton;

    // Injects the logged-in user and lets the concrete dashboard load its own screen data.
    public void setUser(User user) {
        this.currentUser = user;
        this.managementService = new ManagementService(user);
        if (welcomeLabel != null) {
            welcomeLabel.setText("Xush kelibsiz, " + user.getDisplayName() + " (" + user.getRole() + ")");
        }
        afterUserLoaded();
    }

    // Lets each dashboard configure its own tables/forms after login.
    protected abstract void afterUserLoaded();

    // Applies overview numbers when the current dashboard includes summary cards.
    protected void applyOverviewStats(DashboardStats stats) {
        if (employeeCountLabel != null) employeeCountLabel.setText(String.valueOf(stats.getEmployeeCount()));
        if (customerCountLabel != null) customerCountLabel.setText(String.valueOf(stats.getCustomerCount()));
        if (menuCountLabel != null) menuCountLabel.setText(String.valueOf(stats.getMenuItemCount()));
        if (reservationCountLabel != null) reservationCountLabel.setText(String.valueOf(stats.getReservationCount()));
        if (activeOrderCountLabel != null) activeOrderCountLabel.setText(String.valueOf(stats.getActiveOrderCount()));
    }

    // Shows a success or error message in the shared footer status bar.
    protected void setStatus(String message, boolean error) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(message);
        statusLabel.setStyle(error ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #0f766e;");
    }

    // Common null-safe string helper for form/table display.
    protected String safe(String value) {
        return value == null ? "" : value;
    }

    // Common blank check helper used across forms.
    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Common selection guard used by concrete dashboards.
    protected <T> T requireSelection(T value, String entityName) {
        if (value == null) {
            throw new IllegalArgumentException(entityName + " tanlanmagan");
        }
        return value;
    }

    // Returns the user to the login screen and resets the active scene.
    public void logOut() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurantmanagementsystem/login.fxml"));
        Scene scene = new Scene(loader.load(), 720, 520);
        Stage stage = (Stage) logOutButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}