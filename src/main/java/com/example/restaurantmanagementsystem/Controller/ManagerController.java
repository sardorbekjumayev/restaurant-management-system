package com.example.restaurantmanagementsystem.Controller;

import com.example.restaurantmanagementsystem.Model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ManagerController {

    @FXML
    private Label welcomeLabel;

    // Bu metod LoginController dan User kelganda ishlaydi
    public void setUser(User user){
        welcomeLabel.setText("Xush kelibsiz, "+ user.getUsername());
    }

    @FXML
    private Button logOutButton;

    public void logOut(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurantmanagementsystem/login.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) logOutButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}
