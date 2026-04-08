package com.example.librarymanager;

import javafx.application.Platform;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ChatService {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;

    public void connect(String host, int port, Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a background thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String finalMessage = message;
                        // Use Platform.runLater to update the JavaFX UI safely
                        Platform.runLater(() -> onMessageReceived.accept(finalMessage));
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String user, String message) {
        if (out != null) {
            out.println(user + ": " + message);
        }
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}