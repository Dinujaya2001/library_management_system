package controllers;

import dto.MemberDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
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
        String memberId = getTrimmedText(txtMemberId);
        String fullName = getTrimmedText(txtName);
        String email = getTrimmedText(txtEmail);
        String phone = getTrimmedText(txtPhone);
        String address = getTrimmedText(txtAddress);

        if (memberId.isEmpty() || fullName.isEmpty() || email.isEmpty()
                || phone.isEmpty() || address.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please fill in all member details.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a valid email address.");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a valid phone number (7-15 digits).");
            return;
        }

        try {
            if (libraryService.findMemberByCode(memberId) != null) {
                AlertHelper.showWarning(VALIDATION_ERROR, "Member ID '" + memberId + "' is already registered.");
                return;
            }

            MemberDTO memberDto = new MemberDTO(memberId, fullName, email, phone, address);
            libraryService.addMember(memberDto);
            AlertHelper.showInfo("Success", "Member '" + fullName + "' registered successfully.");
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
        TextField[] fields = {txtMemberId, txtName, txtEmail, txtPhone, txtAddress};
        for (TextField field : fields) {
            if (field != null) {
                field.clear();
            }
        }
        if (txtMemberId != null) {
            txtMemberId.requestFocus();
        }
    }

    private String getTrimmedText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }
}