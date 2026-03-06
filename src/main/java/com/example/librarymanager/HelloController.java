package com.example.librarymanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;

public class HelloController {

    // --- Session & Stage Management ---
    public static String loggedInMemberName = "Guest";
    private static Stage primaryStage;

    // --- FXML UI Elements ---
    @FXML private Button btn_user_profile;

    // Login & Signup Popup Elements
    @FXML private TextField loginEmail, signupEmail;
    @FXML private PasswordField loginPass, signupPass;

    // Inventory & Issue Table UI
    @FXML private TableView<Book> table;
    @FXML private TableColumn<Book, String> book_id;
    @FXML private TableColumn<Book, String> book_name;
    @FXML private TableColumn<Book, String> author;
    @FXML private TableColumn<Book, String> status;

    // Inventory Input Fields (onAddBookClick er jonno egulo lagbe)
    @FXML private TextField id;    // fx:id="id"
    @FXML private TextField name;  // fx:id="name"
    @FXML private TextField auth;  // fx:id="auth"

    // Search & Issue Fields
    @FXML private TextField txt_search111; // Book Name Field
    @FXML private TextField txt_search11;  // Author Name Field
    @FXML private TextField txt_search1;   // Issue Book ID Field (Enter Book Id section)
    @FXML private DatePicker submissionDatePicker; // Submission Date Picker
    // Members UI
    @FXML private TableView<Member> memberTable;
    @FXML private TableColumn<Member, String> memname, prof, iss, cont, address;
    @FXML private TextField nam, mail, proff, conta;

    // --- Data Storage & Files ---
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final ObservableList<Member> memberList = FXCollections.observableArrayList();

    private final String BOOK_FILE = "library_data.txt";
    private final String MEMBER_FILE = "members.txt";
    private final String USER_DATA_FILE = "user_data.txt";

    @FXML
    public void initialize() {
        // Load data from files immediately so search/issue functions have data to work with
        loadBooksFromFile();
        loadMembersFromFile();

        // 1. Setup Profile Menu
        if (btn_user_profile != null) setupProfileMenu();

        // 2. Setup Book Table
        if (table != null) {
            // These strings must match the variable names in your Book.java exactly
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));

            table.setItems(bookList);
            table.refresh();
        }

        // 3. Setup Member Table & Pre-fill Email
        if (memberTable != null) {
            memname.setCellValueFactory(new PropertyValueFactory<>("name"));       // getName() খুঁজবে
            prof.setCellValueFactory(new PropertyValueFactory<>("profession"));    // getProfession() খুঁজবে
            iss.setCellValueFactory(new PropertyValueFactory<>("issues"));         // getIssues() খুঁজবে
            cont.setCellValueFactory(new PropertyValueFactory<>("contact"));       // getContact() খুঁজবে
            address.setCellValueFactory(new PropertyValueFactory<>("mail"));       // getMail() খুঁজবে

            memberTable.setItems(memberList);
        }

        // Lock the email field if the user is logged in
        if (mail != null && !loggedInMemberName.equals("Guest")) {
            mail.setText(loggedInMemberName);
            mail.setEditable(false);

            // ব্লকড ইউজার চেক
            Member me = memberList.stream()
                    .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                    .findFirst().orElse(null);

            if (me != null && me.getFineFreq() >= 3) {
                mail.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red;");
                showAlert(Alert.AlertType.ERROR, "Account Blocked",
                        "You have been fined 3+ times. Access restricted for a week.");
            } else {
                mail.setStyle("-fx-opacity: 0.8; -fx-background-color: #d3d3d3; -fx-text-fill: black;");
            }
        }
    }

    // --- 1. AUTHENTICATION & REGISTRATION ---

    @FXML
    public void onLoginClick(ActionEvent event) {
        primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        openPopup("login_popup.fxml", "Bookverse Login");
    }

    @FXML
    public void onSignUpClick(ActionEvent event) {
        openPopup("signup_popup.fxml", "Create Account");
    }

    @FXML
    public void processSignUp(ActionEvent event) {
        String email = signupEmail.getText();
        String pass = signupPass.getText();

        if (email.isEmpty() || pass.isEmpty()) return;

        // Check if user already exists in user_data.txt to prevent duplicate accounts
        if (isUserRegistered(email)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "This Gmail is already registered!");
            return;
        }

        saveToFile(USER_DATA_FILE, email + "," + pass);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
        closePopup(event);
    }

    @FXML
    public void processLogin(ActionEvent event) {
        String email = loginEmail.getText();
        String pass = loginPass.getText();
        boolean authenticated = false;

        File file = new File(USER_DATA_FILE);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String[] creds = scanner.nextLine().split(",");
                    if (creds.length == 2 && creds[0].equals(email) && creds[1].equals(pass)) {
                        authenticated = true;
                        break;
                    }
                }
            } catch (FileNotFoundException e) { e.printStackTrace(); }
        }

        if (authenticated) {
            loggedInMemberName = email;
            closePopup(event);
            updateMainScene("hello-view.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect Gmail or Password.");
        }
    }

    // --- 2. MEMBERSHIP LOGIC (ONE ID PER MEMBER) ---

    @FXML
    public void onBecomeMemberClick() {
        String currentEmail = mail.getText();

        if (nam.getText().isEmpty() || currentEmail.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Incomplete", "Please enter your name.");
            return;
        }

        // Refresh list and check for existing membership
        loadMembersFromFile();
        boolean alreadyExists = memberList.stream()
                .anyMatch(m -> m.getMail().equalsIgnoreCase(currentEmail));

        if (alreadyExists) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Membership");
            alert.setHeaderText(null);
            alert.setContentText("You are already registered as a member!");
            alert.showAndWait();
            return;
        }

        // Adding 8 arguments: name, prof, issues(0), contact, mail, fine(0.0), freq(0), block("none")
        Member newMember = new Member(nam.getText(), proff.getText(), 0, conta.getText(), currentEmail, 0.0, 0, "none");
        memberList.add(newMember);
        saveToFile(MEMBER_FILE, newMember.toString());

        showAlert(Alert.AlertType.INFORMATION, "Welcome", "Membership profile created!");
        nam.clear(); proff.clear(); conta.clear();
    }

    // --- 3. NAVIGATION & UI ---

    private void changeScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void updateMainScene(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            if (primaryStage != null) primaryStage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openPopup(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle(title);
            popupStage.setScene(new Scene(root));
            popupStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void closePopup(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void setupProfileMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem card = new MenuItem("My Profile Card");
        MenuItem logout = new MenuItem("Log Out");
        card.setOnAction(e -> onSeeMyCardClick());
        logout.setOnAction(e -> {
            loggedInMemberName = "Guest";
            updateMainScene("log_out.fxml");
        });
        menu.getItems().addAll(card, new SeparatorMenuItem(), logout);
        btn_user_profile.setOnAction(e -> menu.show(btn_user_profile, Side.BOTTOM, 0, 0));
    }

    @FXML public void onInventoryButtonClick(ActionEvent event) { changeScene(event, "inventory.fxml"); }
    @FXML public void onDashboardButtonClick(ActionEvent event) { changeScene(event, "hello-view.fxml"); }
    @FXML public void onMembersButtonClick(ActionEvent event) { changeScene(event, "members.fxml"); }
    @FXML public void onIssueButtonClick(ActionEvent event) { changeScene(event, "issue.fxml"); }
    @FXML public void onReturnButtonClick(ActionEvent event) { changeScene(event, "return.fxml"); }

    // --- 4. DATA PERSISTENCE ---

    private void saveToFile(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean isUserRegistered(String email) {
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) return false;
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                if (sc.nextLine().split(",")[0].equalsIgnoreCase(email)) return true;
            }
        } catch (Exception e) {}
        return false;
    }

    private void loadBooksFromFile() {
        File file = new File(BOOK_FILE);
        if (!file.exists()) return;
        bookList.clear();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                // Reads 5 fields: ID, Name, Author, Status, TotalIssues
                if (p.length == 5) {
                    bookList.add(new Book(p[0], p[1], p[2], p[3], Integer.parseInt(p[4])));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMembersFromFile() {
        File file = new File(MEMBER_FILE);
        if (!file.exists()) return;
        memberList.clear();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] p = line.split(",");
                if (p.length == 8) {
                    memberList.add(new Member(p[0], p[1], Integer.parseInt(p[2]),
                            p[3], p[4], Double.parseDouble(p[5]), Integer.parseInt(p[6]), p[7]));
                }
            }
            System.out.println("Total members loaded: " + memberList.size()); // চেক করার জন্য
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void onAddBookClick() {
        // Null check jate app crash na kore
        if (id == null || name == null || auth == null) {
            System.out.println("Error: Inventory TextFields are not linked in FXML!");
            return;
        }

        if (id.getText().isEmpty() || name.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "ID and Name cannot be empty!");
            return;
        }

        // Book object toiri kora (ID, Name, Author, Status, TotalIssues)
        Book b = new Book(id.getText(), name.getText(), auth.getText(), "Available", 0);

        bookList.add(b);
        saveToFile(BOOK_FILE, b.toString());

        // Field gulo porishkar kora
        id.clear();
        name.clear();
        auth.clear();

        if (table != null) table.refresh();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Book added successfully!");
    }

    @FXML
    public void onSeeMyCardClick() {
        loadMembersFromFile();
        Member me = memberList.stream()
                .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                .findFirst().orElse(null);

        if (me == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Card Found", "Please register as a member first!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("member-card.fxml"));
            Parent cardRoot = loader.load();
            ((Label) cardRoot.lookup("#cardName")).setText(me.getName());
            ((Label) cardRoot.lookup("#cardProf")).setText(me.getProfession());
            ((Label) cardRoot.lookup("#cardMail")).setText(me.getMail());
            Stage stage = new Stage();
            stage.setScene(new Scene(cardRoot));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // --- SORTING ---
    // --- SORTING LOGIC ---
    @FXML
    public void onSortAuthor(ActionEvent event) {
        table.setItems(bookList); // ফিল্টার থাকলে ফিল্টার রিসেট করে সর্ট হবে
        bookList.sort((b1, b2) -> b1.getAuthor().compareToIgnoreCase(b2.getAuthor()));
    }

    @FXML
    public void onSortAlpha(ActionEvent event) {
        table.setItems(bookList);
        bookList.sort((b1, b2) -> b1.getName().compareToIgnoreCase(b2.getName()));
    }

    @FXML
    public void onSortAvailability(ActionEvent event) {
        table.setItems(bookList);
        bookList.sort((b1, b2) -> b1.getStatus().compareToIgnoreCase(b2.getStatus()));
    }

    // --- SEARCH & POPUP --
    @FXML
    public void handleSmartSearch(ActionEvent event) {
        String nameInput = txt_search111.getText().trim().toLowerCase();
        String authorInput = txt_search11.getText().trim().toLowerCase();

        // ১. যদি শুধু লেখকের নাম দেওয়া হয়: টেবিল ফিল্টার হবে
        if (nameInput.isEmpty() && !authorInput.isEmpty()) {
            ObservableList<Book> filteredList = bookList.filtered(b ->
                    b.getAuthor().toLowerCase().contains(authorInput));
            table.setItems(filteredList);
            if (filteredList.isEmpty()) showAlert(Alert.AlertType.INFORMATION, "Search", "No books found for this author.");
            return;
        }

        // ২. যদি বইয়ের নাম (অথবা নাম + লেখক) দেওয়া হয়: পপআপ আসবে
        Book foundBook = bookList.stream()
                .filter(b -> (b.getName().toLowerCase().contains(nameInput)) &&
                        (authorInput.isEmpty() || b.getAuthor().toLowerCase().contains(authorInput)))
                .findFirst().orElse(null);

        if (foundBook != null) {
            showBookCardPopup(foundBook);
            // পপআপ আসার সাথে সাথে Issue Section-এর Book ID ফিল্ডে আইডি সেট হবে
            if (txt_search1 != null) txt_search1.setText(foundBook.getId());
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Book", "Invalid or unregistered book name.");
        }
    }

    private void showBookCardPopup(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("book-card.fxml"));
            Parent root = loader.load();
            ((Label) root.lookup("#cardBookId")).setText(book.getId());
            ((Label) root.lookup("#cardBookName")).setText(book.getName());
            ((Label) root.lookup("#cardAuthor")).setText(book.getAuthor());
            ((Label) root.lookup("#cardStatus")).setText(book.getStatus());
            ((Label) root.lookup("#cardTotalIssues")).setText(String.valueOf(book.getTotalIssues()));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- EXTENDED ISSUE LOGIC ---
    @FXML
    public void handleIssueBook(ActionEvent event) {
        String bId = txt_search1.getText().trim();

        // তারিখ চেক করা
        if (bId.isEmpty() || submissionDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Fields", "Please enter Book ID and select Submission Date.");
            return;
        }

        Member me = memberList.stream()
                .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                .findFirst().orElse(null);

        if (me == null) {
            showAlert(Alert.AlertType.ERROR, "Auth Error", "Register as a member first!");
            return;
        }

        // --- লজিক চেক ---
        // ১. বকেয়া জরিমানা চেক
        if (me.getFineAmount() > 1000) {
            showAlert(Alert.AlertType.ERROR, "Issue Denied", "Due exceeds 1000 TK. Pay dues first!");
            return;
        }

        // ২. ব্লক চেক (৩ বারের বেশি জরিমানা হলে)
        if (me.getFineFreq() >= 3) {
            showAlert(Alert.AlertType.ERROR, "User Blocked", "You were fined " + me.getFineFreq() + " times. Access blocked for a week!");
            return;
        }

        // ৩. বইয়ের লিমিট চেক (সর্বোচ্চ ৩টি)
        if (me.getIssues() >= 3) {
            showAlert(Alert.AlertType.ERROR, "Limit Exceeded", "You can issue at most 3 books at a time.");
            return;
        }

        Book b = bookList.stream().filter(book -> book.getId().equals(bId)).findFirst().orElse(null);

        if (b != null && b.getStatus().equalsIgnoreCase("Available")) {
            b.setStatus("Issued");
            b.incrementTotalIssues(); // বইয়ের টোটাল ইস্যু বৃদ্ধি
            me.setIssues(me.getIssues() + 1); // ইউজারের টোটাল ইস্যু বৃদ্ধি

            saveAllData();
            table.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Book Issued! Time count started.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Unavailable", "Invalid Book ID or Book is already Issued.");
        }
    }

    private void saveAllData() {
        // ওভাররাইট মোডে সেভ করা হচ্ছে যাতে পুরনো ডাটা মুছে নতুন স্ট্যাটাস আপডেট হয়
        try (PrintWriter bw = new PrintWriter(new FileWriter(BOOK_FILE, false))) {
            for (Book b : bookList) bw.println(b.toString());
        } catch (IOException e) { e.printStackTrace(); }

        try (PrintWriter mw = new PrintWriter(new FileWriter(MEMBER_FILE, false))) {
            for (Member m : memberList) mw.println(m.toString());
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML
    public void handleReturnBook(ActionEvent event) {
        String bId = txt_search1.getText().trim(); // Return section-এও Book ID ইনপুট নিতে হবে

        if (bId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "Please enter the Book ID to return.");
            return;
        }

        // ১. বই খুঁজে বের করা
        Book b = bookList.stream().filter(book -> book.getId().equals(bId)).findFirst().orElse(null);
        if (b == null || !b.getStatus().equalsIgnoreCase("Issued")) {
            showAlert(Alert.AlertType.ERROR, "Error", "This book is not currently issued.");
            return;
        }

        // ২. মেম্বার খুঁজে বের করা (যেহেতু লগইন করা ইউজার ফেরত দিচ্ছে)
        Member me = memberList.stream()
                .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                .findFirst().orElse(null);

        if (me != null) {
            // ৩. জরিমানা ক্যালকুলেশন লজিক (উদাহরণস্বরূপ ৭ দিন পার হলে ১০০ টাকা)
            // বাস্তবে এখানে Submission Date এবং বর্তমান তারিখের পার্থক্য বের করতে হবে
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate dueDate = submissionDatePicker.getValue(); // ইস্যু করার সময় সেট করা ডেট

            if (dueDate != null && today.isAfter(dueDate)) {
                long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                int weeksLate = (int) Math.ceil(daysLate / 7.0);
                double fine = weeksLate * 100.0;

                me.setFineAmount(me.getFineAmount() + fine);
                me.setFineFreq(me.getFineFreq() + 1); // জরিমানা খাওয়ার ফ্রিকুয়েন্সি ১ বাড়ানো হলো

                showAlert(Alert.AlertType.INFORMATION, "Fine Applied",
                        "You are " + daysLate + " days late. Fine: " + fine + " TK added to your account.");
            }

            // ৪. স্ট্যাটাস আপডেট
            b.setStatus("Available");
            me.setIssues(Math.max(0, me.getIssues() - 1)); // ইউজারের একটি বই কমলো

            saveAllData();
            table.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully!");
        }
    }
}

