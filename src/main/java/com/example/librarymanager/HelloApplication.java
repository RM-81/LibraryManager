package com.example.librarymanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 1. Point this to your login fxml file
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("log_out.fxml"));

        // 2. Load the scene (936x558 matches your HBox pref sizes)
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Bookverse - Login");
        stage.setResizable(false); // Keeps your layout looking perfect
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
