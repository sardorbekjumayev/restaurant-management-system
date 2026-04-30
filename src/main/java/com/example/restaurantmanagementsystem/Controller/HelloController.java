package com.example.restaurantmanagementsystem.Controller;

import com.example.restaurantmanagementsystem.Model.User;
import com.example.restaurantmanagementsystem.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    private final AuthService authService = new AuthService();

    @FXML
    private TextField loginField;
    @FXML
    private TextField passwordField;
    @FXML
    private ChoiceBox<String> roleBox;
    @FXML
    private Label loginMessageLabel;

    @FXML
    public void initialize() {
        roleBox.getSelectionModel().selectFirst();
    }

    @FXML
    protected void signInButton(ActionEvent event) throws IOException {
        try {
            String role = roleBox.getValue();
            User user = authService.login(loginField.getText().trim(), passwordField.getText().trim(), role);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurantmanagementsystem/ManagerDashboard.fxml"));
            Parent root = loader.load();

            ManagerController managerController = loader.getController();
            managerController.setUser(user);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1320, 860));
            stage.setTitle(user.getRole() + " Dashboard");
            stage.show();
        } catch (Exception e) {
            loginMessageLabel.setText(e.getMessage());
        }
    }
}
