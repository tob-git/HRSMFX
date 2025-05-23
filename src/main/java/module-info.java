module com.example.hrsm2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.hrsm2 to javafx.fxml;
    opens com.example.hrsm2.controller to javafx.fxml;
    opens com.example.hrsm2.model to javafx.fxml, javafx.base;
    opens com.example.hrsm2.gui to javafx.fxml;
    
    exports com.example.hrsm2;
    exports com.example.hrsm2.controller;
    exports com.example.hrsm2.model;
    exports com.example.hrsm2.gui;
}