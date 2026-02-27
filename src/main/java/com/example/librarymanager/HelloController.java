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

    // Inventory UI
    @FXML private TableView<Book> table;
    @FXML private TableColumn<Book, String> book_id, book_name, author, status;
    @FXML private TextField id, name, auth;

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
        // 1. Setup Profile Menu
        if (btn_user_profile != null) setupProfileMenu();

        // 2. Setup Book Table
        if (table != null) {
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));
            loadBooksFromFile();
            table.setItems(bookList);
        }

        // 3. Setup Member Table & Pre-fill Email for Membership
        if (memname != null) {
            memname.setCellValueFactory(new PropertyValueFactory<>("name"));
            prof.setCellValueFactory(new PropertyValueFactory<>("profession"));
            iss.setCellValueFactory(new PropertyValueFactory<>("issues"));
            cont.setCellValueFactory(new PropertyValueFactory<>("contact"));
            address.setCellValueFactory(new PropertyValueFactory<>("mail"));
            loadMembersFromFile();
            if (memberTable != null) memberTable.setItems(memberList);

            // Logic to lock the email to the logged-in user
            if (mail != null && !loggedInMemberName.equals("Guest")) {
                mail.setText(loggedInMemberName);
                mail.setEditable(false);
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

        Member newMember = new Member(nam.getText(), proff.getText(), "0", conta.getText(), currentEmail);
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
                if (p.length == 4) bookList.add(new Book(p[0], p[1], p[2], p[3]));
            }
        } catch (Exception e) {}
    }

    private void loadMembersFromFile() {
        File file = new File(MEMBER_FILE);
        if (!file.exists()) return;
        memberList.clear();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                if (p.length == 5) memberList.add(new Member(p[0], p[1], p[2], p[3], p[4]));
            }
        } catch (Exception e) {}
    }

    @FXML
    public void onAddBookClick() {
        if (id.getText().isEmpty() || name.getText().isEmpty()) return;
        Book b = new Book(id.getText(), name.getText(), auth.getText(), "Available");
        bookList.add(b);
        saveToFile(BOOK_FILE, b.toString());
        id.clear(); name.clear(); auth.clear();
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
}