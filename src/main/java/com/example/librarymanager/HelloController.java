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
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

import java.io.*;
import java.util.Scanner;

public class HelloController {

    // --- Session & Stage Management ---
    public static String loggedInMemberName = "Guest";
    private static Stage primaryStage;

    // --- FXML UI Elements ---
    @FXML
    private Button btn_user_profile;

    // Login & Signup Popup Elements
    @FXML
    private TextField loginEmail, signupEmail;
    @FXML
    private PasswordField loginPass, signupPass;
    @FXML
    private TableView<Book> inventoryTable;
    @FXML
    private TableView<Book> issueTable;
    @FXML
    private TableView<Book> returnTable;


    // Inventory & Issue Table UI
    @FXML
    private TableColumn<Book, String> book_id;
    @FXML
    private TableColumn<Book, String> book_name;
    @FXML
    private TableColumn<Book, String> author;
    @FXML
    private TableColumn<Book, String> status;
    @FXML
    private TableColumn<Book, Double> bookFineColumn;

    // Inventory Input Fields (onAddBookClick er jonno egulo lagbe)
    @FXML
    private TextField id;    // fx:id="id"
    @FXML
    private TextField name;  // fx:id="name"
    @FXML
    private TextField auth;  // fx:id="auth"

    // Search & Issue Fields
    @FXML
    private TextField txt_search111; // Book Name Field
    @FXML
    private TextField txt_search11;  // Author Name Field
    @FXML
    private TextField txt_search1;   // Issue Book ID Field (Enter Book Id section)
    @FXML
    private DatePicker submissionDatePicker; // Submission Date Picker
    // Members UI
    @FXML
    private TableView<Member> memberTable;
    @FXML
    private TableColumn<Member, String> memname, prof, iss, cont, address;
    @FXML
    private TextField nam, mail, proff, conta;

    @FXML
    private TableView<Book> table1; // Due fines table
    @FXML
    private TableColumn<Book, String> book_id1, book_name1, author1, sta1;
    @FXML
    private TextField txt_search2; // Return Book Name (Auto-fill)
    @FXML
    private TextField txt_search3; // Pay Book ID
    @FXML
    private TextField txt_search4; // Pay Book Name Search
    @FXML
    private TextField txt_payamount; // Pay Amount
    private final ObservableList<Book> fineList = FXCollections.observableArrayList();

    // --- Data Storage & Files ---
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final ObservableList<Member> memberList = FXCollections.observableArrayList();

    private final String BOOK_FILE = "library_data.txt";
    private final String MEMBER_FILE = "members.txt";
    private final String USER_DATA_FILE = "user_data.txt";

    @FXML
    public void initialize() {
        loadBooksFromFile();
        loadMembersFromFile();

        if (btn_user_profile != null) setupProfileMenu();


        if (inventoryTable != null) {
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));
            inventoryTable.setItems(bookList);
        }


        if (issueTable != null) {
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));


            issueTable.setItems(bookList);
        }


        if (returnTable != null) {

            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(cellData -> {
                Book b = cellData.getValue();
                if (b.getDueDate() == null || b.getIssuedTo().equals("none")) {
                    return new SimpleStringProperty("Available");
                }

                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), b.getDueDate());

                if (daysBetween < 0) {
                    return new SimpleStringProperty("Overdue by " + Math.abs(daysBetween) + " days");
                } else {
                    return new SimpleStringProperty(daysBetween + " days remaining");
                }
            });


            returnTable.setItems(bookList.filtered(b ->
                    b.getIssuedTo() != null && b.getIssuedTo().equalsIgnoreCase(loggedInMemberName)
            ));
        }


        if (table1 != null) {
            book_id1.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name1.setCellValueFactory(new PropertyValueFactory<>("name"));
            if (bookFineColumn != null) {
                bookFineColumn.setCellValueFactory(new PropertyValueFactory<>("bookFine"));
            }


            table1.setItems(bookList.filtered(b ->
                    b.getIssuedTo() != null &&
                            b.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                            b.getBookFine() > 0
            ));
        }


        if (memberTable != null) {
            memname.setCellValueFactory(new PropertyValueFactory<>("name"));
            prof.setCellValueFactory(new PropertyValueFactory<>("profession"));
            iss.setCellValueFactory(new PropertyValueFactory<>("issues"));
            cont.setCellValueFactory(new PropertyValueFactory<>("contact"));
            address.setCellValueFactory(new PropertyValueFactory<>("mail"));

            memberTable.setItems(memberList);
            memberTable.refresh();
        }

        if (mail != null && !loggedInMemberName.equals("Guest")) {
            mail.setText(loggedInMemberName);
        }

        setupListeners();
    }

    private void setupListeners() {

        if (txt_search1 != null) {
            txt_search1.textProperty().addListener((obs, old, newValue) -> {
                Book b = bookList.stream()
                        .filter(book -> book.getId().equals(newValue) && book.getIssuedTo().equals(loggedInMemberName))
                        .findFirst().orElse(null);
                if (txt_search2 != null) txt_search2.setText(b != null ? b.getName() : "");
            });
        }


        if (txt_search2 != null) {
            txt_search2.textProperty().addListener((obs, old, newValue) -> {
                Book b = bookList.stream()
                        .filter(book -> book.getName().equalsIgnoreCase(newValue) && book.getIssuedTo().equals(loggedInMemberName))
                        .findFirst().orElse(null);
                if (txt_search1 != null && b != null) txt_search1.setText(b.getId());
            });
        }


        if (txt_search4 != null) {
            txt_search4.textProperty().addListener((obs, old, newValue) -> {

                Book b = bookList.stream()
                        .filter(book -> book.getName().equalsIgnoreCase(newValue) &&
                                book.getIssuedTo().equals("none") &&
                                book.getBookFine() > 0)
                        .findFirst().orElse(null);
                if (txt_search3 != null) txt_search3.setText(b != null ? b.getId() : "");
            });
        }
    }
    public double getGlobalTotalDue() {
        return memberList.stream().mapToDouble(Member::getMemberTotalDue).sum();
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (authenticated) {
            loggedInMemberName = email;
            closePopup(event);
            updateMainScene("hello-view.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect Gmail or Password.");
        }
    }


    @FXML
    public void onBecomeMemberClick() {
        String currentEmail = mail.getText();

        if (nam.getText().isEmpty() || currentEmail.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Incomplete", "Please enter your name.");
            return;
        }


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


        Member newMember = new Member(nam.getText(), proff.getText(), 0, conta.getText(), currentEmail, 0.0, 0, "none", 0.0, 0.0, 0, "none");
        memberList.add(newMember);
        saveToFile(MEMBER_FILE, newMember.toString());

        showAlert(Alert.AlertType.INFORMATION, "Welcome", "Membership profile created!");
        nam.clear();
        proff.clear();
        conta.clear();
    }

    // --- 3. NAVIGATION & UI ---

    private void changeScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMainScene(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            if (primaryStage != null) primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void closePopup(ActionEvent event) {
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

    @FXML
    public void onInventoryButtonClick(ActionEvent event) {
        changeScene(event, "inventory.fxml");
    }

    @FXML
    public void onDashboardButtonClick(ActionEvent event) {
        changeScene(event, "hello-view.fxml");
    }

    @FXML
    public void onMembersButtonClick(ActionEvent event) {
        changeScene(event, "members.fxml");
    }

    @FXML
    public void onIssueButtonClick(ActionEvent event) {
        changeScene(event, "issue.fxml");
    }
    @FXML
    public void onReturnButtonClick(ActionEvent event) {
        changeScene(event, "return.fxml");
        refreshReturnTables();
    }

    // --- 4. DATA PERSISTENCE ---

    private void saveToFile(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserRegistered(String email) {
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) return false;
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                if (sc.nextLine().split(",")[0].equalsIgnoreCase(email)) return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private void loadBooksFromFile() {
        File file = new File(BOOK_FILE);
        if (!file.exists()) return;
        bookList.clear();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] p = line.split(",");
                if (p.length == 9) {

                    java.time.LocalDate issDate = p[5].equals("none") ? null : java.time.LocalDate.parse(p[5]);
                    java.time.LocalDate dDate = p[6].equals("none") ? null : java.time.LocalDate.parse(p[6]);

                    bookList.add(new Book(
                            p[0], p[1], p[2], p[3],
                            Integer.parseInt(p[4]),
                            issDate, dDate,
                            Double.parseDouble(p[7]),
                            p[8]
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMembersFromFile() {
        File file = new File(MEMBER_FILE);
        if (!file.exists()) return;
        memberList.clear();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                if (p.length == 12) {
                    memberList.add(new Member(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4], Double.parseDouble(p[5]),
                            Integer.parseInt(p[6]), p[7], Double.parseDouble(p[8]), Double.parseDouble(p[9]),
                            Integer.parseInt(p[10]), p[11]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onAddBookClick() {

        if (inventoryTable == null || id == null || name == null || auth == null) {
            System.out.println("Error: Inventory Elements are not linked in FXML!");
            return;
        }

        if (id.getText().isEmpty() || name.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "ID and Name cannot be empty!");
            return;
        }

        Book b = new Book(
                id.getText(),
                name.getText(),
                auth.getText(),
                "Available",
                0,
                null, // Issue Date
                null, // Due Date
                0.0,  // Fine
                "none" // IssuedTo
        );

        bookList.add(b);


        saveAllData();


        id.clear();
        name.clear();
        auth.clear();


        inventoryTable.refresh();

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
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    // --- SORTING LOGIC (REPLACED) ---
    @FXML
    public void onSortAuthor(ActionEvent event) {

        bookList.sort((b1, b2) -> b1.getAuthor().compareToIgnoreCase(b2.getAuthor()));


        if (inventoryTable != null) inventoryTable.setItems(bookList);
        if (issueTable != null) issueTable.setItems(bookList);

        refreshAllTables();
    }

    @FXML
    public void onSortAlpha(ActionEvent event) {
        bookList.sort((b1, b2) -> b1.getName().compareToIgnoreCase(b2.getName()));


        if (inventoryTable != null) inventoryTable.setItems(bookList);
        if (issueTable != null) issueTable.setItems(bookList);

        refreshAllTables();
    }

    @FXML
    public void onSortAvailability(ActionEvent event) {
        bookList.sort((b1, b2) -> b1.getStatus().compareToIgnoreCase(b2.getStatus()));

        if (inventoryTable != null) inventoryTable.setItems(bookList);
        if (issueTable != null) issueTable.setItems(bookList);

        refreshAllTables();
    }

    private void refreshAllTables() {
        if (inventoryTable != null) inventoryTable.refresh();
        if (issueTable != null) issueTable.refresh();
    }

    // --- SEARCH & POPUP --
    @FXML
    public void handleSmartSearch(ActionEvent event) {
        String nameInput = txt_search111.getText().trim().toLowerCase();
        String authorInput = txt_search11.getText().trim().toLowerCase();


        if (nameInput.isEmpty() && authorInput.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter Book Name or Author Name.");
            return;
        }


        ObservableList<Book> filteredList = bookList.filtered(b ->
                (nameInput.isEmpty() || b.getName().toLowerCase().contains(nameInput)) &&
                        (authorInput.isEmpty() || b.getAuthor().toLowerCase().contains(authorInput))
        );


        if (inventoryTable != null) {
            inventoryTable.setItems(filteredList);
        }

        if (issueTable != null) {
            issueTable.setItems(filteredList);
        }


        if (!nameInput.isEmpty()) {
            Book foundBook = filteredList.stream().findFirst().orElse(null);
            if (foundBook != null) {
                showBookCardPopup(foundBook);
                if (txt_search1 != null) txt_search1.setText(foundBook.getId());
            }
        }

        if (filteredList.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Search", "No books found matching your search.");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- EXTENDED ISSUE LOGIC ---
    @FXML
    public void handleIssueBook(ActionEvent event) {
        String bId = txt_search1.getText().trim();
        java.time.LocalDate selectedDate = submissionDatePicker.getValue();

        if (bId.isEmpty() || selectedDate == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Fields", "Please enter Book ID and select Submission Date.");
            return;
        }


        loadMembersFromFile();
        Member me = memberList.stream()
                .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                .findFirst().orElse(null);

        if (me == null) {
            showAlert(Alert.AlertType.ERROR, "Auth Error", "Register as a member first!");
            return;
        }


        if (me.getIssues() >= 3) {
            showAlert(Alert.AlertType.ERROR, "Limit Exceeded", "You already have 3 books issued. Return one to issue another!");
            return;
        }

        if (me.getMemberTotalDue() > 1000) {
            showAlert(Alert.AlertType.ERROR, "Denied", "Total Due > 1000 TK. Pay dues first!");
            return;
        }


        Book b = bookList.stream().filter(book -> book.getId().equals(bId)).findFirst().orElse(null);

        if (b != null && b.getStatus().equalsIgnoreCase("Available")) {

            b.setStatus("Issued");
            b.setIssuedTo(loggedInMemberName);
            b.setIssueDate(java.time.LocalDate.now());
            b.setDueDate(selectedDate);
            b.setTotalIssues(b.getTotalIssues() + 1);


            me.setIssues(me.getIssues() + 1);


            saveAllData();


            if (inventoryTable != null) inventoryTable.refresh();
            if (issueTable != null) issueTable.refresh();
            if (returnTable != null) {
                returnTable.setItems(bookList.filtered(book ->
                        book.getIssuedTo().equalsIgnoreCase(loggedInMemberName) && book.getStatus().equalsIgnoreCase("Issued")));
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Book Issued successfully! Current Issues: " + me.getIssues());
        } else {
            showAlert(Alert.AlertType.ERROR, "Unavailable", "Invalid ID or Book already issued.");
        }
    }

    private void saveAllData() {

        try (PrintWriter bw = new PrintWriter(new FileWriter(BOOK_FILE, false))) {
            for (Book b : bookList) bw.println(b.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter mw = new PrintWriter(new FileWriter(MEMBER_FILE, false))) {
            for (Member m : memberList) mw.println(m.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleReturnBook(ActionEvent event) {
        String bId = txt_search1.getText().trim();

        Book b = bookList.stream()
                .filter(book -> book.getId().equals(bId))
                .findFirst()
                .orElse(null);

        if (b != null && b.getIssuedTo().equalsIgnoreCase(loggedInMemberName) && b.getStatus().equalsIgnoreCase("Issued")) {


            Member me = memberList.stream()
                    .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                    .findFirst().orElse(null);

            LocalDate today = LocalDate.now();
            double fine = 0;


            if (b.getDueDate() != null && today.isAfter(b.getDueDate())) {
                long daysLate = java.time.temporal.ChronoUnit.DAYS.between(b.getDueDate(), today);
                fine = daysLate * 50.0;
                b.setBookFine(fine);
                if (me != null) {
                    me.setMemberTotalDue(me.getMemberTotalDue() + fine);
                }
            } else {
                b.setBookFine(0.0);
            }


            if (me != null && me.getIssues() > 0) {
                me.setIssues(me.getIssues() - 1);
            }


            b.setStatus("Available");
            b.setIssuedTo("none");
            b.setIssueDate(null);
            b.setDueDate(null);

            saveAllData();
            refreshReturnTables();

            txt_search1.clear();
            txt_search2.clear();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Book Returned! Now you can issue another book.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Book ID or not issued to you!");
        }
    }
    private void refreshReturnTables() {
        if (returnTable != null) {
            // Upore shudhu jegulo ekhon hate ache (Issued status)
            returnTable.setItems(bookList.filtered(book ->
                    book.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                            book.getStatus().equalsIgnoreCase("Issued")));
        }

        if (table1 != null) {

            table1.setItems(bookList.filtered(book ->
                    book.getStatus().equalsIgnoreCase("Available") &&
                            book.getBookFine() > 0));
        }
    }

    @FXML
    public void handlePayFine(ActionEvent event) {
        String enteredId = txt_search3.getText().trim();
        String amountStr = txt_payamount.getText().trim();


        if (enteredId.isEmpty() || amountStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter Book ID and Amount.");
            return;
        }


        Book b = bookList.stream()
                .filter(book -> book.getId().equals(enteredId) && book.getBookFine() > 0)
                .findFirst().orElse(null);

        if (b == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID", "No pending fine found for this Book ID.");
            return;
        }

        try {
            double payAmount = Double.parseDouble(amountStr);
            Member me = memberList.stream()
                    .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                    .findFirst().orElse(null);

            if (me != null) {

                if (payAmount <= 0 || payAmount > b.getBookFine()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Amount must be between 1 and " + b.getBookFine());
                    return;
                }


                b.setBookFine(b.getBookFine() - payAmount);
                me.setMemberTotalPaid(me.getMemberTotalPaid() + payAmount);
                me.setMemberTotalDue(Math.max(0, me.getMemberTotalDue() - payAmount));


                String historyEntry = java.time.LocalDate.now() + ": Paid " + payAmount + " TK for Book ID " + b.getId();

                me.setPaymentHistory(me.getPaymentHistory() + " | " + historyEntry);


                saveAllData();


                if (table1 != null) {

                    table1.setItems(bookList.filtered(book -> book.getBookFine() > 0));
                }
                if (memberTable != null) memberTable.refresh();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Payment of " + payAmount + " TK successful!");


                txt_search3.clear();
                txt_payamount.clear();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid numeric amount.");
        }
    }

}

