package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import service.LibraryService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    private final LibraryService libraryService = new LibraryService();

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label lblTotalBooks;

    @FXML
    private Label lblTotalMembers;

    @FXML
    private Label lblBorrowedBooks;

    @FXML
    private Label lblOverdueBooks;

    private Node dashboardCenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (mainPane != null) {
            dashboardCenter = mainPane.getCenter();
        }
        refreshDashboardCounts();
    }

    public void refreshDashboardCounts() {
        try {
            if (lblTotalBooks != null) {
                lblTotalBooks.setText(String.valueOf(libraryService.getTotalBooks()));
            }
            if (lblTotalMembers != null) {
                lblTotalMembers.setText(String.valueOf(libraryService.getTotalMembers()));
            }
            if (lblBorrowedBooks != null) {
                lblBorrowedBooks.setText(String.valueOf(libraryService.getCurrentlyBorrowedCount()));
            }
            if (lblOverdueBooks != null) {
                lblOverdueBooks.setText(String.valueOf(libraryService.getOverdueCount()));
            }
        } catch (SQLException e) {
            AlertHelper.showWarning("Database Notice",
                    "Could not refresh dashboard statistics: " + e.getMessage());
        }
    }

    private void loadPage(String fxmlFileName) {
        try {
            URL fxmlUrl = getClass().getResource("/view/" + fxmlFileName + ".fxml");
            if (fxmlUrl == null) {
                AlertHelper.showError("Page Not Found",
                        "Missing FXML file: /view/" + fxmlFileName + ".fxml");
                return;
            }
            Parent view = FXMLLoader.load(fxmlUrl);
            mainPane.setCenter(view);
        } catch (IOException e) {
            AlertHelper.showError("Navigation Error",
                    "Failed to load " + fxmlFileName + ": " + e.getMessage());
        }
    }

    @FXML
    void btnDashboardOnAction() {
        if (mainPane != null && dashboardCenter != null) {
            mainPane.setCenter(dashboardCenter);
        }
        refreshDashboardCounts();
    }

    @FXML
    void btnAddBookOnAction() {
        loadPage("add_book_page");
    }

    @FXML
    void btnAddMemberOnAction() {
        loadPage("add_member_page");
    }

    @FXML
    void btnManageMembersOnAction() {
        loadPage("manage_members_page");
    }

    @FXML
    void btnIssueBookOnAction() {
        loadPage("issue_book_page");
    }

    @FXML
    void btnReturnBookOnAction() {
        loadPage("return_book_page");
    }

    @FXML
    void btnHistoryBookOnAction() {
        loadPage("history_page");
    }

    @FXML
    void btnLogoutOnAction() {
        boolean confirmed = AlertHelper.showConfirmation("Confirm Logout",
                "Are you sure you want to log out?");
        if (confirmed && mainPane != null && mainPane.getScene() != null) {
            try {
                Stage stage = (Stage) mainPane.getScene().getWindow();
                SceneNavigator.switchScene(stage, "login_page.fxml", "Library Management System - Login");
            } catch (IOException e) {
                AlertHelper.showError("Logout Error", "Failed to load login page: " + e.getMessage());
            }
        }
    }
}