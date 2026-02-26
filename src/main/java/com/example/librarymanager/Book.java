package com.example.librarymanager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Book {
    // We use StringProperty for better compatibility with JavaFX TableViews
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty author;
    private final StringProperty status;

    public Book(String id, String name, String author, String status) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.author = new SimpleStringProperty(author);
        this.status = new SimpleStringProperty(status);
    }

    // --- Getters (Required by PropertyValueFactory) ---

    // If your column factory is new PropertyValueFactory<>("id")
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    // If your column factory is new PropertyValueFactory<>("book_name")
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    // If your column factory is new PropertyValueFactory<>("author")
    public String getAuthor() { return author.get(); }
    public StringProperty authorProperty() { return author; }

    // If your column factory is new PropertyValueFactory<>("status")
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    // --- Setters ---
    public void setId(String value) { id.set(value); }
    public void setName(String value) { name.set(value); }
    public void setAuthor(String value) { author.set(value); }
    public void setStatus(String value) { status.set(value); }

    /**
     * Helper for saving to a file in CSV format
     */
    @Override
    public String toString() {
        return getId() + "," + getName() + "," + getAuthor() + "," + getStatus();
    }
}