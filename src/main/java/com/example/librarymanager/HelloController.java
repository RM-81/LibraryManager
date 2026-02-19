package com.example.librarymanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class HelloController {

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colIsbn;

    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextField txtISBN;

    // This list holds the data for the table
    private ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Link Table Columns to the Book class properties
        // Ensure these "title", "author" match the variable names in Book.java
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        bookTable.setItems(bookList);
    }

    @FXML
    protected void onAddBookClick() {
        // Create a new book from the text field inputs
        Book newBook = new Book(txtTitle.getText(), txtAuthor.getText(), txtISBN.getText(), "Available");

        // Add it to the list
        bookList.add(newBook);

        // Clear the fields for the next entry
        txtTitle.clear();
        txtAuthor.clear();
        txtISBN.clear();
    }
}
