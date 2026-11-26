package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import model.Member;
import service.LibraryService;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class AddMemberPageController {

    private static final String VALIDATION_ERROR = "Validation Error";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-\\s]{7,15}$");

    private final LibraryService libraryService = new LibraryService();

    @FXML
    private TextField txtMemberId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtAddress;

    @FXML
    void btnRegisterMemberOnAction() {
        String memberId = txtMemberId != null && txtMemberId.getText() != null ? txtMemberId.getText().trim() : "";
        String fullName = txtName != null && txtName.getText() != null ? txtName.getText().trim() : "";
        String email = txtEmail != null && txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        String phone = txtPhone != null && txtPhone.getText() != null ? txtPhone.getText().trim() : "";
        String address = txtAddress != null && txtAddress.getText() != null ? txtAddress.getText().trim() : "";

        if (memberId.isEmpty() || fullName.isEmpty() || email.isEmpty()
                || phone.isEmpty() || address.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please fill in all member fields.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a valid email address.");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a valid phone number.");
            return;
        }

        Member member = new Member(memberId, fullName, email, phone, address);

        try {
            libraryService.addMember(member);
            AlertHelper.showInfo("Success", "Member registered successfully.");
            clearFields();
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to register member: " + e.getMessage());
        }
    }

    @FXML
    void btnClearMemberOnAction() {
        clearFields();
    }

    private void clearFields() {
        if (txtMemberId != null) {
            txtMemberId.clear();
        }
        if (txtName != null) {
            txtName.clear();
        }
        if (txtEmail != null) {
            txtEmail.clear();
        }
        if (txtPhone != null) {
            txtPhone.clear();
        }
        if (txtAddress != null) {
            txtAddress.clear();
        }
        if (txtMemberId != null) {
            txtMemberId.requestFocus();
        }
    }
}