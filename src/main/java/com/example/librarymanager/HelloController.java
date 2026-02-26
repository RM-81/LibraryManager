package com.example.librarymanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloController {

    // This method handles the actual switching
    public void changeScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This is the specific action for your Inventory button
    @FXML
    public void onInventoryButtonClick(ActionEvent event) {
        // Change "inventory-view.fxml" to whatever your second file is named!
        changeScene(event, "inventory.fxml");
    }

    @FXML
    public void onDashboardButtonClick(ActionEvent event) {
        changeScene(event, "hello-view.fxml");
    }

}
