package com.example.librarymanager;

public class Member {
    private String name;
    private String profession;
    private int issues;
    private String contact;
    private String mail;
    private double fine;          // General fine field
    private int freq;           // Frequency
    private String blockStatus;
    private double memberTotalPaid;
    private double memberTotalDue;
    private int finedCount;
    private String paymentHistory;


    public Member(String name, String profession, int issues, String contact, String mail,
                  double fine, int freq, String blockStatus, double memberTotalPaid,
                  double memberTotalDue, int finedCount, String paymentHistory) {
        this.name = name;
        this.profession = profession;
        this.issues = issues;
        this.contact = contact;
        this.mail = mail;
        this.fine = fine;
        this.freq = freq;
        this.blockStatus = blockStatus;
        this.memberTotalPaid = memberTotalPaid;
        this.memberTotalDue = memberTotalDue;
        this.finedCount = finedCount;
        this.paymentHistory = paymentHistory;
    }

    // --- Getters and Setters ---
    public String getName() { return name; }
    public String getProfession() { return profession; }
    public int getIssues() { return issues; }
    public void setIssues(int issues) { this.issues = issues; }
    public String getContact() { return contact; }
    public String getMail() { return mail; }

    public double getMemberTotalDue() { return memberTotalDue; }
    public void setMemberTotalDue(double memberTotalDue) { this.memberTotalDue = memberTotalDue; }

    public double getMemberTotalPaid() { return memberTotalPaid; }
    public void setMemberTotalPaid(double memberTotalPaid) { this.memberTotalPaid = memberTotalPaid; }

    public String getBlockStatus() { return blockStatus; }
    public void setBlockStatus(String blockStatus) { this.blockStatus = blockStatus; }

    public int getFinedCount() { return finedCount; }
    public void setFinedCount(int finedCount) { this.finedCount = finedCount; }

    public int getTotalReturnsCount() { return freq; } // freq কে রিটার্ন কাউন্ট হিসেবে ব্যবহার করছি
    public void setTotalReturnsCount(int count) { this.freq = count; }

    public String getPaymentHistory() { return (paymentHistory == null || paymentHistory.equals("none")) ? "" : paymentHistory; }
    public void setPaymentHistory(String history) { this.paymentHistory = history; }


    @Override
    public String toString() {
        return name + "," + profession + "," + issues + "," + contact + "," + mail + "," +
                fine + "," + freq + "," + blockStatus + "," + memberTotalPaid + "," +
                memberTotalDue + "," + finedCount + "," + (paymentHistory.isEmpty() ? "none" : paymentHistory);
    }
}