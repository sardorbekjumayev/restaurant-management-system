package com.example.restaurantmanagementsystem;

import com.example.restaurantmanagementsystem.DAO.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            VBox root = new VBox(12);
            root.setStyle("-fx-padding: 24; -fx-background-color: #fafaf9;");
            Label title = new Label("MySQL ulanishida xatolik");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            Label message = new Label(
                    "MySQL serverni ishga tushiring va src/main/resources/application.properties dagi db.url, db.username, db.password qiymatlarini tekshiring.\n\n"
                            + e.getMessage()
            );
            message.setWrapText(true);
            root.getChildren().addAll(title, message);
            stage.setScene(new Scene(root, 760, 260));
            stage.setTitle("Database Error");
            stage.show();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 720, 520);
        stage.setTitle("Restaurant Management System");
        stage.setScene(scene);
        stage.show();
    }
}
