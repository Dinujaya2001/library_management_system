package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginPageController {

    private final LoginController loginController = new LoginController();

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    void btnOnActionLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username == null || username.isBlank()) {
            AlertHelper.showWarning("Validation Error", "Please enter your username.");
            txtUsername.requestFocus();
            return;
        }

        if (password == null || password.isBlank()) {
            AlertHelper.showWarning("Validation Error", "Please enter your password.");
            txtPassword.requestFocus();
            return;
        }

        if (loginController.checkNameAndUserPassword(username, password)) {
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                SceneNavigator.switchScene(stage, "home_page.fxml", "Library Management System - Dashboard");
            } catch (IOException e) {
                AlertHelper.showError("Load Error", "Failed to load Home Page: " + e.getMessage());
            }
        } else {
            AlertHelper.showError("Login Failed", "Invalid username or password.");
            txtPassword.clear();
            txtPassword.requestFocus();
        }
    }

    @FXML
    void btnOnActionClear(ActionEvent event) {
        txtUsername.clear();
        txtPassword.clear();
        txtUsername.requestFocus();
    }
}