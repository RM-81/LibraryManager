package com.example.librarymanager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Member {
    private String name;
    private String profession;
    private int issues;
    private String contact;
    private String mail;
    private double fineAmount;
    private int fineFreq;
    private String blockDate;

    // কনস্ট্রাক্টর আপনার যা আছে তাই থাকবে...

    // --- এই মেথডগুলো অবশ্যই থাকতে হবে ---
    public String getName() { return name; }
    public String getProfession() { return profession; }
    public int getIssues() { return issues; }
    public String getContact() { return contact; } // এইটা মিসিং ছিল
    public String getMail() { return mail; }
    public double getFineAmount() { return fineAmount; }
    public int getFineFreq() { return fineFreq; }
    public String getBlockDate() { return blockDate; }

    // ডাটা সেভ এবং আপডেটের জন্য সেটার (Setters)
    public void setIssues(int issues) { this.issues = issues; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public void setFineFreq(int fineFreq) { this.fineFreq = fineFreq; }

    @Override
    public String toString() {
        return name + "," + profession + "," + issues + "," + contact + "," + mail + "," + fineAmount + "," + fineFreq + "," + blockDate;
    }
    public Member(String name, String profession, int issues, String contact, String mail, double fineAmount, int fineFreq, String blockDate) {
        this.name = name;
        this.profession = profession;
        this.issues = issues;
        this.contact = contact;
        this.mail = mail;
        this.fineAmount = fineAmount;
        this.fineFreq = fineFreq;
        this.blockDate = blockDate;
    }


}