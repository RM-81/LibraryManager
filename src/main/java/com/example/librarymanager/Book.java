package com.example.librarymanager;

import javafx.beans.property.SimpleStringProperty;

public class Book {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty author;
    private final SimpleStringProperty status;

    public Book(String id, String name, String author, String status) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.author = new SimpleStringProperty(author);
        this.status = new SimpleStringProperty(status);
    }

    // Getters are required for the TableView to find the data
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getAuthor() { return author.get(); }
    public String getStatus() { return status.get(); }
}
