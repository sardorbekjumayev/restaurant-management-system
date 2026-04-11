module com.example.restaurantmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.restaurantmanagementsystem to javafx.fxml;
    exports com.example.restaurantmanagementsystem;
}