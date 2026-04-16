package com.example.restaurantmanagementsystem.Controller;

import com.example.restaurantmanagementsystem.Model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ManagerController {

    @FXML
    private Label welcomeLabel;

    // Bu metod LoginController dan User kelganda ishlaydi
    public void setUser(User user){
        welcomeLabel.setText("Xush kelibsiz, "+ user.getUsername());
    }

}
