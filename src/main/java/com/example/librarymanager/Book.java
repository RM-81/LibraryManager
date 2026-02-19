package com.example.librarymanager;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private String status;

    public Book(String title, String author, String isbn, String status) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.status = status;
    }

    // Getters are required for the TableView to "see" the data
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getStatus() { return status; }
}
