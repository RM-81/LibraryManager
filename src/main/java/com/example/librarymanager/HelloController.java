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
    private static Stage primaryStage; // Reference to the main window for scene switching

    // --- FXML UI Elements ---
    @FXML private Button btn_user_profile;

    // Login Popup Elements
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPass;

    // Inventory UI
    @FXML private TableView<Book> table;
    @FXML private TableColumn<Book, String> book_id, book_name, author, status;
    @FXML private TextField id, name, auth;

    // Members UI
    @FXML private TableView<Member> memberTable;
    @FXML private TableColumn<Member, String> memname, prof, iss, cont, address;
    @FXML private TextField nam, mail, proff, conta;

    // --- Data Storage ---
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final ObservableList<Member> memberList = FXCollections.observableArrayList();

    private final String BOOK_FILE = "library_data.txt";
    private final String MEMBER_FILE = "members.txt";

    @FXML
    public void initialize() {
        // 1. Setup Profile Dropdown
        if (btn_user_profile != null) {
            setupProfileMenu();
        }

        // 2. Setup Inventory Table
        if (table != null) {
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));
            loadBooksFromFile();
            table.setItems(bookList);
        }

        // 3. Setup Member Table
        if (memname != null) {
            memname.setCellValueFactory(new PropertyValueFactory<>("name"));
            prof.setCellValueFactory(new PropertyValueFactory<>("profession"));
            iss.setCellValueFactory(new PropertyValueFactory<>("issues"));
            cont.setCellValueFactory(new PropertyValueFactory<>("contact"));
            address.setCellValueFactory(new PropertyValueFactory<>("mail"));
            loadMembersFromFile();
            if (memberTable != null) {
                memberTable.setItems(memberList);
            }
        }
    }

    // --- 1. POPUP & AUTHENTICATION LOGIC ---

    @FXML
    public void onLoginClick(ActionEvent event) {
        // Save the reference to the Main Stage (the landing page window)
        primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_popup.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Locks the background
            popupStage.setTitle("Bookverse Login");
            popupStage.setScene(new Scene(root));
            popupStage.show();
        } catch (IOException e) {
            System.err.println("Could not load login-popup.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    public void processLogin(ActionEvent event) {
        String email = loginEmail.getText();
        String pass = loginPass.getText(); // You can add password check logic here

        loadMembersFromFile();

        // Find member by Email
        Member user = memberList.stream()
                .filter(m -> m.getMail().equalsIgnoreCase(email))
                .findFirst().orElse(null);

        if (user != null) {
            loggedInMemberName = user.getName();

            // Close the Popup Window
            Stage currentPopup = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentPopup.close();

            // Switch the Main Window to Dashboard
            updateMainScene("hello-view.fxml");
            System.out.println("Login success: " + loggedInMemberName);
        } else {
            System.err.println("Login Failed: User not found in members.txt");
        }
    }

    @FXML
    public void closePopup(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // --- 2. PROFILE & LOGOUT ---

    private void setupProfileMenu() {
        ContextMenu profileMenu = new ContextMenu();
        MenuItem profile = new MenuItem("My Profile");
        MenuItem logout = new MenuItem("Log Out");

        profile.setOnAction(e -> onSeeMyCardClick());

        logout.setOnAction(e -> {
            loggedInMemberName = "Guest";
            updateMainScene("log_out.fxml"); // Redirect back to landing page
            System.out.println("User logged out.");
        });

        profileMenu.getItems().addAll(profile, new SeparatorMenuItem(), logout);
        btn_user_profile.setOnAction(event -> profileMenu.show(btn_user_profile, Side.BOTTOM, 0, 0));
    }

    // --- 3. NAVIGATION ---

    @FXML public void onInventoryButtonClick(ActionEvent event) { changeScene(event, "inventory.fxml"); }
    @FXML public void onDashboardButtonClick(ActionEvent event) { changeScene(event, "hello-view.fxml"); }
    @FXML public void onMembersButtonClick(ActionEvent event) { changeScene(event, "members.fxml"); }
    @FXML public void onIssueButtonClick(ActionEvent event) { changeScene(event, "issue.fxml"); }
    @FXML public void onReturnButtonClick(ActionEvent event) { changeScene(event, "return.fxml"); }

    @FXML public void onSignUpClick(ActionEvent event) { changeScene(event, "members.fxml"); }

    private void changeScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile);
        }
    }

    private void updateMainScene(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- 4. DATA ACTIONS & FILE PERSISTENCE ---

    @FXML
    public void onAddBookClick() {
        if (id.getText().isEmpty() || name.getText().isEmpty()) return;
        Book newBook = new Book(id.getText(), name.getText(), auth.getText(), "Available");
        bookList.add(newBook);
        saveToFile(BOOK_FILE, newBook.toString());
        id.clear(); name.clear(); auth.clear();
    }

    @FXML
    public void onBecomeMemberClick() {
        if (nam.getText().isEmpty()) return;
        Member newMember = new Member(nam.getText(), proff.getText(), "0", conta.getText(), mail.getText());
        memberList.add(newMember);
        saveToFile(MEMBER_FILE, newMember.toString());
        nam.clear(); proff.clear(); conta.clear(); mail.clear();
    }

    @FXML
    public void onSeeMyCardClick() {
        loadMembersFromFile();
        Member me = memberList.stream()
                .filter(m -> m.getName().equalsIgnoreCase(loggedInMemberName))
                .findFirst().orElse(null);

        if (me == null) return;

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

    private void saveToFile(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadBooksFromFile() {
        File file = new File(BOOK_FILE);
        if (!file.exists()) return;
        bookList.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] p = scanner.nextLine().split(",");
                if (p.length == 4) bookList.add(new Book(p[0], p[1], p[2], p[3]));
            }
        } catch (Exception e) { }
    }

    private void loadMembersFromFile() {
        File file = new File(MEMBER_FILE);
        if (!file.exists()) return;
        memberList.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] p = scanner.nextLine().split(",");
                if (p.length == 5) memberList.add(new Member(p[0], p[1], p[2], p[3], p[4]));
            }
        } catch (Exception e) { }
    }
}