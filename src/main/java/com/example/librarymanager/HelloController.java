package com.example.librarymanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;

public class HelloController {

    // --- Session Management ---
    // This simulates the user currently using the application
    public static String loggedInMemberName = "Rajib Ahammed";

    // --- FXML UI Elements: Inventory ---
    @FXML private TableView<Book> table;
    @FXML private TableColumn<Book, String> book_id, book_name, author, status;
    @FXML private TextField id, name, auth;

    // --- FXML UI Elements: Members ---
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
        // --- 1. SETUP INVENTORY TABLE ---
        // Checks if 'table' exists (meaning we are on inventory.fxml)
        if (table != null) {
            book_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            book_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            author.setCellValueFactory(new PropertyValueFactory<>("author"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));

            loadBooksFromFile();
            table.setItems(bookList);
        }

        // --- 2. SETUP MEMBER TABLE ---
        // Checks if 'memname' exists (meaning we are on members.fxml)
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

    // --- Navigation Logic ---
    @FXML public void onInventoryButtonClick(ActionEvent event) { changeScene(event, "inventory.fxml"); }
    @FXML public void onDashboardButtonClick(ActionEvent event) { changeScene(event, "hello-view.fxml"); }
    @FXML public void onMembersButtonClick(ActionEvent event) { changeScene(event, "members.fxml"); }
    @FXML public void onIssueButtonClick(ActionEvent event) { changeScene(event, "issue.fxml"); }
    @FXML public void onReturnButtonClick(ActionEvent event) { changeScene(event, "return.fxml"); }

    private void changeScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile);
            e.printStackTrace();
        }
    }

    // --- Inventory Actions ---
    @FXML
    public void onAddBookClick() {
        if (id.getText().isEmpty() || name.getText().isEmpty()) return;

        Book newBook = new Book(id.getText(), name.getText(), auth.getText(), "Available");
        bookList.add(newBook);
        saveToFile(BOOK_FILE, newBook.toString());

        id.clear(); name.clear(); auth.clear();
    }

    // --- Member Actions ---
    @FXML
    public void onBecomeMemberClick() {
        if (nam.getText().isEmpty()) return;

        // Default "0" issues for new members
        Member newMember = new Member(nam.getText(), proff.getText(), "0", conta.getText(), mail.getText());
        memberList.add(newMember);
        saveToFile(MEMBER_FILE, newMember.toString());

        nam.clear(); proff.clear(); conta.clear(); mail.clear();
    }

    @FXML
    public void onSeeMyCardClick() {
        // 1. Ensure the list is fresh
        loadMembersFromFile();

        // 2. Find the logged-in member
        Member me = null;
        for (Member m : memberList) {
            if (m.getName().equalsIgnoreCase(loggedInMemberName)) {
                me = m;
                break;
            }
        }

        if (me == null) {
            System.out.println("Member '" + loggedInMemberName + "' not found in records.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("member-card.fxml"));
            Parent cardRoot = loader.load();

            // Populate labels in the card using lookup
            ((Label) cardRoot.lookup("#cardName")).setText(me.getName());
            ((Label) cardRoot.lookup("#cardProf")).setText(me.getProfession());
            ((Label) cardRoot.lookup("#cardMail")).setText(me.getMail());

            Stage stage = new Stage();
            stage.setTitle("Digital ID Card");
            stage.setScene(new Scene(cardRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Universal File Logic ---
    private void saveToFile(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) { e.printStackTrace(); }
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
        } catch (Exception e) { e.printStackTrace(); }
    }
}