module com.example.restaurantmanagementsystem {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.example.restaurantmanagementsystem to javafx.fxml;
    exports com.example.restaurantmanagementsystem;
    exports com.example.restaurantmanagementsystem.Controller;
    opens com.example.restaurantmanagementsystem.Controller to javafx.fxml;
    opens com.example.restaurantmanagementsystem.Model.Users to javafx.base;
    opens com.example.restaurantmanagementsystem.Model.Restaurant to javafx.base;
    opens com.example.restaurantmanagementsystem.Model.Tables to javafx.base;
    opens com.example.restaurantmanagementsystem.Model.Orders to javafx.base;
    opens com.example.restaurantmanagementsystem.Model.Payments to javafx.base;
    exports com.example.restaurantmanagementsystem.Model;
    opens com.example.restaurantmanagementsystem.Model to javafx.base, javafx.fxml;
}
