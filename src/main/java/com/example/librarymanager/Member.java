package com.example.librarymanager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Member {
    private final StringProperty name;
    private final StringProperty profession;
    private final StringProperty issues;
    private final StringProperty contact;
    private final StringProperty mail;

    public Member(String name, String profession, String issues, String contact, String mail) {
        this.name = new SimpleStringProperty(name);
        this.profession = new SimpleStringProperty(profession);
        this.issues = new SimpleStringProperty(issues);
        this.contact = new SimpleStringProperty(contact);
        this.mail = new SimpleStringProperty(mail);
    }

    // Getters for TableView (matches PropertyValueFactory names)
    public String getName() { return name.get(); }
    public String getProfession() { return profession.get(); }
    public String getIssues() { return issues.get(); }
    public String getContact() { return contact.get(); }
    public String getMail() { return mail.get(); }

    @Override
    public String toString() {
        return getName() + "," + getProfession() + "," + getIssues() + "," + getContact() + "," + getMail();
    }
}
