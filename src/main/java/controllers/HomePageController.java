package controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class HomePageController {
    @FXML
    private BorderPane mainPane;


    private void loadPage(String fxmlFileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/view/" + fxmlFileName + ".fxml"));
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void btnAddBookOnAction(ActionEvent event) {
        loadPage("add_book_page");
    }

    @FXML
    void btnHistoryBookOnAction(ActionEvent event) {
        loadPage("history_page");

    }

    @FXML
    void btnIssueBookOnAction(ActionEvent event) {
        loadPage("issue_book_page");

    }

    @FXML
    void btnManageMembersOnAction(ActionEvent event) {
        loadPage("manage_members_page");

    }

    @FXML
    void btnReturnBookOnAction(ActionEvent event) {
        loadPage("return_book_page");



    }



}
