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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;


import java.io.*;
import java.util.Scanner;

import java.awt.Desktop;
import java.net.URI;
import javafx.scene.input.MouseEvent;

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

    // Labels for System-wide and Personal Stats [cite: 1, 2]
    @FXML private Label total_issues, my_issues;
    @FXML private Label total_returns, my_returns;
    @FXML private Label total_fines, my_fines;
    @FXML private Label total_donations; // Total books added [cite: 1, 2]
    @FXML private Label total_dues, my_dues;

    // Progress Bars [cite: 1, 2]
    @FXML private ProgressBar issue_progress, return_progress, fine_progress, due_progress;

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
        updateDashboard();

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
            // 1. Column for ID, Name, and Author
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));

            // 2. Status column showing Overdue info
            status.setCellValueFactory(cellData -> {
                Book b = cellData.getValue();
                // If the book is not issued, just show Available
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

            // 3. THE FIX: Update the filter to only show "Issued" books
            // This ensures that once status is changed to "Available" in handleReturnBook,
            // it automatically vanishes from this table.
            returnTable.setItems(bookList.filtered(b ->
                    b.getIssuedTo() != null &&
                            b.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                            b.getStatus().equalsIgnoreCase("Issued") // <--- ADD THIS LINE
            ));
        }


        if (table1 != null) {
            // 1. Basic Column Bindings
            book_id1.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name1.setCellValueFactory(new PropertyValueFactory<>("name"));
            author1.setCellValueFactory(new PropertyValueFactory<>("author"));

            // 2. Overdue Days Column (sta1)
            sta1.setCellValueFactory(cellData -> {
                Book b = cellData.getValue();
                // If there's no due date, it was never issued/overdue
                if (b.getDueDate() == null) return new SimpleStringProperty("0");

                LocalDate today = LocalDate.now();
                // If the book is still "Issued", calculate days relative to Today
                // If the book is "Available" (Returned), we show the final overdue count
                if (b.getStatus().equalsIgnoreCase("Issued") && today.isAfter(b.getDueDate())) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(b.getDueDate(), today);
                    return new SimpleStringProperty(String.valueOf(days));
                } else if (b.getBookFine() > 0) {
                    // If returned but fine exists, show days based on the 60 TK rate
                    long frozenDays = (long) (b.getBookFine() / 60.0);
                    return new SimpleStringProperty(String.valueOf(frozenDays));
                }
                return new SimpleStringProperty("0");
            });

            // 3. Fine Column (Shows the 60 TK/day rate)
            bookFineColumn.setCellValueFactory(cellData -> {
                Book b = cellData.getValue();
                LocalDate today = LocalDate.now();
                double displayFine = b.getBookFine();

                // LIVE UPDATE: If the book is still out and overdue, keep increasing the fine
                if (b.getStatus().equalsIgnoreCase("Issued") && b.getDueDate() != null && today.isAfter(b.getDueDate())) {
                    long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(b.getDueDate(), today);
                    displayFine = overdueDays * 60.0;
                    b.setBookFine(displayFine); // Sync the object
                }

                return new SimpleDoubleProperty(displayFine).asObject();
            });

            // 4. THE FILTER: This keeps the book in the table even after return
            table1.setItems(bookList.filtered(b ->
                    b.getIssuedTo() != null &&
                            b.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                            (b.getBookFine() > 0 || (b.getDueDate() != null && LocalDate.now().isAfter(b.getDueDate())))
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

    private void updateDashboard() {
        if (total_issues == null) return;

        Member me = memberList.stream()
                .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                .findFirst().orElse(null);

        // 1. ISSUES: Total system-wide issues vs My lifetime issues
        int sysIss = bookList.stream().mapToInt(Book::getTotalIssues).sum();
        int myIss = (me != null) ? me.getIssues() : 0;
        total_issues.setText(String.valueOf(sysIss));
        my_issues.setText(" " + myIss);
        issue_progress.setProgress(sysIss == 0 ? 0 : (double) myIss / sysIss);

        // 2. DUES & PAID: Personal only (Total Due by me vs Amount Paid by me)
        double myDue = (me != null) ? me.getMemberTotalDue() : 0.0;
        double myPaid = (me != null) ? me.getMemberTotalPaid() : 0.0;
        total_dues.setText(String.format("%.0f", myDue));
        my_dues.setText(" " + (int)myPaid);
        // Progress: Ratio of how much of your total debt you have cleared
        double totalDebtEver = myDue + myPaid;
        due_progress.setProgress(totalDebtEver == 0 ? 0 :(double) myPaid / totalDebtEver);

        // 3. FINES: How many times fine occurred for ALL vs how many times for ME
        int sysFineCount = memberList.stream().mapToInt(Member::getFinedCount).sum();
        int myFineCount = (me != null) ? me.getFinedCount() : 0;
        total_fines.setText(String.valueOf(sysFineCount));
        my_fines.setText(" " + myFineCount);
        fine_progress.setProgress(sysFineCount == 0 ? 0 : (double) myFineCount / sysFineCount);

        // 4. RETURNS: Lifetime System vs Lifetime Me
        int sysRet = memberList.stream().mapToInt(Member::getTotalReturnsCount).sum();
        int myRet = (me != null) ? me.getTotalReturnsCount() : 0;
        total_returns.setText(String.valueOf(sysRet));
        my_returns.setText(" " + myRet);
        return_progress.setProgress(sysRet == 0 ? 0 : (double) myRet / sysRet);

        // 5. DONATIONS: Total books in system
        total_donations.setText(String.valueOf(bookList.size()));
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
                table1.setItems(bookList.filtered(b ->
                        b.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                                b.getBookFine() > 0 &&
                                (newValue == null || newValue.isEmpty() || b.getName().toLowerCase().contains(newValue.toLowerCase()))
                ));
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
            loggedInMemberName = loginEmail.getText();;
            updateDashboard();
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
        bookList.clear();
        try (Scanner scanner = new Scanner(new File(BOOK_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                // It must handle exactly 9 parts to match your Book constructor
                if (parts.length == 9) {
                    Book book = new Book(
                            parts[0], // ID
                            parts[1], // Name
                            parts[2], // Author
                            parts[3], // Status
                            Integer.parseInt(parts[4]), // Total Issues
                            parts[5].equals("none") ? null : LocalDate.parse(parts[5]), // Issue Date
                            parts[6].equals("none") ? null : LocalDate.parse(parts[6]), // Due Date
                            Double.parseDouble(parts[7]), // Fine (CRITICAL FIELD)
                            parts[8]  // Issued To
                    );
                    bookList.add(book);
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
        updateDashboard();


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


        // Calculate active issues by filtering the current book list
        long activeIssuesCount = bookList.stream()
                .filter(book -> book.getIssuedTo() != null &&
                        book.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                        book.getStatus().equalsIgnoreCase("Issued"))
                .count();

        if (activeIssuesCount >= 3) {
            showAlert(Alert.AlertType.ERROR, "Limit Exceeded", "You currently have " + activeIssuesCount + " books out. Return one to issue another!");
            return;
        }

        if (me.getMemberTotalDue() > 1000) {
            showAlert(Alert.AlertType.ERROR, "Denied", "Total Due > 1000 TK. Pay dues first!");
            return;
        }


        Book b = bookList.stream().filter(book -> book.getId().equals(bId)).findFirst().orElse(null);

        if (b != null && b.getStatus().equalsIgnoreCase("Available")) {

            b.setStatus("Issued");
            b.setIssuedTo(me.getMail());
            b.setIssueDate(LocalDate.now());
            b.setDueDate(submissionDatePicker.getValue());

// --- CUMULATIVE UPDATE ---
            b.incrementTotalIssues();      // Book's lifetime count
            me.setIssues(me.getIssues() + 1); // Member's lifetime count
// -------------------------

            saveAllData();
            updateDashboard(); // Ensure UI reflects the new totals


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
    private void handleReturnBook() {
        String bookId = txt_search1.getText().trim();

        // 1. Find the book
        Book b = bookList.stream()
                .filter(book -> book.getId().equalsIgnoreCase(bookId))
                .findFirst().orElse(null);

        // 2. Check if the book is currently issued
        if (b != null && b.getStatus().equalsIgnoreCase("Issued")) {
            LocalDate today = LocalDate.now();
            double calculatedFine = 0;

            // 3. Find the Member who issued the book
            Member me = memberList.stream()
                    .filter(m -> m.getMail().equalsIgnoreCase(b.getIssuedTo()))
                    .findFirst().orElse(null);

            // 4. Calculate Individual Fine (50 TK per day)
            if (b.getDueDate() != null && today.isAfter(b.getDueDate())) {
                long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(b.getDueDate(), today);
                calculatedFine = overdueDays * 60.0;
                b.setBookFine(calculatedFine);


                // Update Member's total debt
                if (me != null) {
                    me.setFinedCount(me.getFinedCount() + 1);
                    me.setMemberTotalDue(me.getMemberTotalDue() + calculatedFine);
                }
            }

            // 5. FIX: Decrease the Member's active issue count

            // 6. Update Book Status to Available
            b.setStatus("Available");


            // 7. Save while issuedTo is still linked (to keep the fine record)


            me.setTotalReturnsCount(me.getTotalReturnsCount() + 1); // Increment lifetime returns

            saveAllData();
            updateDashboard(); // Refresh counts and progress bars

            // 8. Clear issue details from the book
            b.setIssueDate(null);
            b.setDueDate(null);
            // Keep issuedTo for the fine table; only set to "none" if fine is 0
            if (calculatedFine <= 0) {
                b.setIssuedTo("none");
            }

            saveAllData(); // Final save

            // 9. UI REFRESH: Make book disappear from Return Table immediately
            if (returnTable != null) {
                returnTable.setItems(bookList.filtered(book ->
                        book.getIssuedTo() != null &&
                                book.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                                book.getStatus().equalsIgnoreCase("Issued")
                ));
                returnTable.refresh();
            }

            // 10. UI REFRESH: Show the new fine in the Fines Table (table1)
            if (table1 != null) {
                table1.setItems(bookList.filtered(book ->
                        book.getIssuedTo() != null &&
                                book.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                                (LocalDate.now().isAfter(book.getDueDate()) || book.getBookFine() > 0)
                ));
                table1.refresh();
            }

            // Calculate active issues for the message
            long activeLeft = bookList.stream()
                    .filter(book -> book.getIssuedTo() != null &&
                            book.getIssuedTo().equalsIgnoreCase(loggedInMemberName) &&
                            book.getStatus().equalsIgnoreCase("Issued"))
                    .count();

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Book returned. Fine: " + calculatedFine + " TK. Active books remaining: " + activeLeft);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Book ID or book is not issued.");
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
    private void handlePayFine() {
        try {
            String bookId = txt_search3.getText().trim();
            String amountStr = txt_payamount.getText().trim();

            if (bookId.isEmpty() || amountStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter both Book ID and Amount.");
                return;
            }

            double payAmount = Double.parseDouble(amountStr);
            Book b = bookList.stream()
                    .filter(book -> book.getId().equalsIgnoreCase(bookId))
                    .findFirst().orElse(null);

            if (b == null) {
                showAlert(Alert.AlertType.ERROR, "Not Found", "No book found with ID: " + bookId);
                return;
            }

            // --- BLOCKING MECHANISM ---
            // Check if the book is still issued (not yet returned)
            if (b.getStatus().equalsIgnoreCase("Issued")) {
                showAlert(Alert.AlertType.ERROR, "Return Book First",
                        "You cannot pay the fine while the book is still with you.\n" +
                                "Please return the book first to freeze the fine amount.");
                return;
            }
            // ---------------------------

            if (payAmount > b.getBookFine()) {
                showAlert(Alert.AlertType.ERROR, "Overpayment", "You cannot pay more than the fine amount.");
                return;
            }

            Member me = memberList.stream()
                    .filter(m -> m.getMail().equalsIgnoreCase(loggedInMemberName))
                    .findFirst().orElse(null);

            if (me != null) {
                // Update Financials
                b.setBookFine(b.getBookFine() - payAmount);
                me.setMemberTotalPaid(me.getMemberTotalPaid() + payAmount);
                me.setMemberTotalDue(Math.max(0, me.getMemberTotalDue() - payAmount));

                // If fully paid, clear the link
                if (b.getBookFine() <= 0) {
                    b.setIssuedTo("none");
                    b.setDueDate(null);
                }

                saveAllData();
                updateDashboard();

                if (table1 != null) {
                    table1.refresh();
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Payment accepted. Remaining: " + b.getBookFine() + " TK.");
                txt_search3.clear();
                txt_payamount.clear();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid numeric amount.");
        }
    }

    @FXML
    private void handleone(MouseEvent event) {
        String url = "https://online.fliphtml5.com/shbfot/qcvx/#p=10";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Laptop/Windows specific fallback
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            System.out.println("Could not open browser: " + e.getMessage());
        }
    }

    @FXML
    private void handletwo(MouseEvent event) {
        String url = "https://online.anyflip.com/kcdpv/ecxn/mobile/index.html";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Laptop/Windows specific fallback
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            System.out.println("Could not open browser: " + e.getMessage());
        }
    }

    @FXML
    private void handlethree(MouseEvent event) {
        String url = "https://online.fliphtml5.com/wqjwb/dvbo/#p=1";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Laptop/Windows specific fallback
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            System.out.println("Could not open browser: " + e.getMessage());
        }
    }
}

