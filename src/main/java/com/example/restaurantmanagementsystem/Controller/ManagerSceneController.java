package com.example.restaurantmanagementsystem.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ManagerSceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void switchToSceneAddMenu(ActionEvent actionEvent) throws IOException {
        Parent root= FXMLLoader.load(getClass().getResource("AddMenu.fxml"));
    }
    public void switchToSceneAddMenuItem(ActionEvent actionEvent) {
        stage.setScene(scene);
        stage.show();
    }
    public void switchToSceneRegisterNewAccount(ActionEvent actionEvent) {
        stage.setScene(scene);
        stage.show();
    }
    public void switchToSceneCancelAccount(ActionEvent actionEvent) {
        stage.setScene(scene);
        stage.show();
    }
    public void switchToSceneLogOut(ActionEvent actionEvent) {
        stage.setScene(scene);
    }


}
