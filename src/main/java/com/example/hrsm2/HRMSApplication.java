package com.example.hrsm2;

import com.example.hrsm2.util.SampleDataLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HRMSApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load sample data
        SampleDataLoader.loadSampleData();
        
        // Load login view instead of main view
        FXMLLoader fxmlLoader = new FXMLLoader(HRMSApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("HRMS Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
} 