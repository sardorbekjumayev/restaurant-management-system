package com.example.restaurantmanagementsystem.Controller;

import com.example.restaurantmanagementsystem.Model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    private TextField loginField;
    @FXML
    private TextField passwordField;
    @FXML
    private ChoiceBox roleBox;

    @FXML
    protected void signInButton(ActionEvent event) throws IOException {

        String roleofuser = roleBox.getValue().toString();
        String loginoguser = loginField.getText();
        String passwordofuser = passwordField.getText();

        User user = new User(loginoguser, passwordofuser, roleofuser);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurantmanagementsystem/ManagerDashboard.fxml"));
        Parent root = loader.load();

        // 3️⃣ DashboardController-ni olish
        ManagerController managerController = loader.getController();

        // 4️⃣ User ma’lumotini yuborish
        managerController.setUser(user);

        // 5️⃣ Sahifani almashtirish
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.show();

    }
}
