package com.example.librarymanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatController {

    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;

    private ChatService chatService;
    // Using the logged-in name from your main controller [cite: 3]
    private String currentUser = HelloController.loggedInMemberName;

    @FXML
    public void initialize() {
        chatService = new ChatService();
        // Connecting to localhost for testing. Change to server IP for network use.
        chatService.connect("localhost", 12345, message -> {
            chatArea.appendText(message + "\n");
        });

        chatArea.appendText("--- Welcome to Bookverse Community Chat ---\n");
    }

    @FXML
    private void handleSendMessage() {
        String msg = messageField.getText();
        if (msg != null && !msg.trim().isEmpty()) {
            chatService.sendMessage(currentUser, msg);
            messageField.clear();
        }
    }

    @FXML
    private void onBackButtonClick(ActionEvent event) {
        try {
            chatService.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}