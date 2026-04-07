package com.example.librarymanager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class Book {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty author;
    private final StringProperty status;
    private int totalIssues;

    // Fine and Tracker Fields
    private LocalDate issueDate;
    private LocalDate dueDate;
    private double bookFine;
    private String issuedTo;

    // Updated Constructor to handle all fields
    public Book(String id, String name, String author, String status, int totalIssues,
                LocalDate issueDate, LocalDate dueDate, double bookFine, String issuedTo) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.author = new SimpleStringProperty(author);
        this.status = new SimpleStringProperty(status);
        this.totalIssues = totalIssues;


        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.bookFine = bookFine;
        this.issuedTo = issuedTo;
    }

    // --- Getters and Setters ---
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setStatus(String value) { this.status.set(value); }

    public double getBookFine() { return bookFine; }
    public void setBookFine(double bookFine) { this.bookFine = bookFine; }

    public String getIssuedTo() { return issuedTo; }
    public void setIssuedTo(String issuedTo) { this.issuedTo = issuedTo; }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getAuthor() { return author.get(); }
    public StringProperty authorProperty() { return author; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public int getTotalIssues() { return totalIssues; }
    public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }

    public void incrementTotalIssues() { this.totalIssues++; }

    /**
     * Replace this: Updated to save 9 fields to the file
     */
    @Override
    public String toString() {
        String iDate = (issueDate == null) ? "none" : issueDate.toString();
        String dDate = (dueDate == null) ? "none" : dueDate.toString();

        // Ensure all 9 fields are saved to the file in this exact order
        return getId() + "," +
                getName() + "," +
                getAuthor() + "," +
                getStatus() + "," +
                getTotalIssues() + "," +
                iDate + "," +
                dDate + "," +
                getBookFine() + "," +
                getIssuedTo();
    }
}