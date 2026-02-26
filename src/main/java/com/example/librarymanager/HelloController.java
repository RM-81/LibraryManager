package com.example.librarymanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;

public class HelloController {

    // --- FXML UI Elements ---
    @FXML private TableView<Book> table;
    @FXML private TableColumn<Book, String> book_id, book_name, author, status;
    @FXML private TextField id, name, auth;

    // --- Data Storage ---
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final String FILE_NAME = "library_data.txt";

    /**
     * initialize() runs automatically whenever the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Only run this if we are on the Inventory page (where the table exists)
        if (table != null) {
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));


            loadBooksFromFile();
            table.setItems(bookList);
        }
    }

    // --- Navigation Logic ---

    @FXML
    public void onInventoryButtonClick(ActionEvent event) {
        changeScene(event, "inventory.fxml");
    }

    @FXML
    public void onDashboardButtonClick(ActionEvent event) {
        changeScene(event, "hello-view.fxml");
    }

    @FXML
    public void onIssueButtonClick(ActionEvent event) {
        changeScene(event, "issue.fxml");
    }

    @FXML
    public void onMembersButtonClick(ActionEvent event) {
        changeScene(event, "members.fxml");
    }

    @FXML
    public void onReturnButtonClick(ActionEvent event) {
        changeScene(event, "return.fxml");
    }



    private void changeScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile);
            e.printStackTrace();
        }
    }

    // --- File & Inventory Logic ---

    @FXML
    public void onAddBookClick() {
        // 1. Create the new Book object from TextFields
        Book newBook = new Book(id.getText(), name.getText(), auth.getText(), "Available");

        // 2. Add to the list (updates the TableView immediately)
        bookList.add(newBook);

        // 3. Save to text file
        saveBookToFile(newBook);

        // 4. Clear fields
        id.clear();
        name.clear();
        auth.clear();
    }

    private void saveBookToFile(Book book) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            // Saves as: ID,Name,Author,Status
            writer.write(book.getId() + "," + book.getName() + "," + book.getAuthor() + "," + book.getStatus());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBooksFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        bookList.clear(); // Clear list before loading to prevent duplicates
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    bookList.add(new Book(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}