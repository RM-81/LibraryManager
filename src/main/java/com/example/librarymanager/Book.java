package com.example.librarymanager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Book {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty author;
    private final StringProperty status;
    private int totalIssues; // NEW: Added to track popularity for the popup

    public Book(String id, String name, String author, String status, int totalIssues) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.author = new SimpleStringProperty(author);
        this.status = new SimpleStringProperty(status);
        this.totalIssues = totalIssues;
    }

    // --- Getters ---
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getAuthor() { return author.get(); }
    public StringProperty authorProperty() { return author; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public int getTotalIssues() { return totalIssues; } // NEW

    // --- Setters ---
    public void setId(String value) { id.set(value); }
    public void setName(String value) { name.set(value); }
    public void setAuthor(String value) { author.set(value); }
    public void setStatus(String value) { status.set(value); }
    public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; } // NEW

    // Increments issue count by 1 when the Issue button is clicked
    public void incrementTotalIssues() {
        this.totalIssues++;
    }

    /**
     * Updated for saving: ID, Name, Author, Status, TotalIssues
     */
    @Override
    public String toString() {
        return getId() + "," + getName() + "," + getAuthor() + "," + getStatus() + "," + totalIssues;
    }
}