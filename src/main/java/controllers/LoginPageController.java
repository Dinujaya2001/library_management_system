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
        if (txtUsername == null || txtPassword == null) {
            AlertHelper.showError("FXML Binding Error",
                    "Login form fields are not bound. Verify fx:id=\"txtUsername\", fx:id=\"txtPassword\", and fx:controller=\"controllers.LoginPageController\".");
            return;
        }

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

        String trimmedUsername = username.trim();

        if (loginController.checkNameAndUserPassword(trimmedUsername, password)) {
            try {
                Stage stage = resolveStage(event);
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
        if (event != null && txtUsername != null) {
            txtUsername.clear();
        }
        if (txtPassword != null) {
            txtPassword.clear();
        }
        if (txtUsername != null) {
            txtUsername.requestFocus();
        }
    }

    private Stage resolveStage(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node sourceNode && sourceNode.getScene() != null) {
            return (Stage) sourceNode.getScene().getWindow();
        }
        if (btnLogin != null && btnLogin.getScene() != null) {
            return (Stage) btnLogin.getScene().getWindow();
        }
        return null;
    }
}