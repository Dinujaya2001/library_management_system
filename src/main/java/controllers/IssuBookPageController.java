package controllers;

import dto.BookDTO;
import dto.MemberDTO;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import service.LibraryService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;

public class IssuBookPageController {

    private static final String DATABASE_ERROR = "Database Error";
    private static final String VALIDATION_ERROR = "Validation Error";
    private static final String NOT_FOUND_TITLE = "Not Found";
    private static final long DEFAULT_LOAN_DAYS = 14L;
    private static final long MAX_LOAN_DAYS = 90L;

    private final LibraryService libraryService = new LibraryService();

    @FXML
    private TextField txtMemberId;

    @FXML
    private Label lblMemberDetails;

    @FXML
    private TextField txtBookId;

    @FXML
    private Label lblBookDetails;

    @FXML
    private DatePicker dpDueDate;

    @FXML
    public void initialize() {
        if (dpDueDate != null && dpDueDate.getValue() == null) {
            dpDueDate.setValue(LocalDate.now(ZoneId.systemDefault()).plusDays(DEFAULT_LOAN_DAYS));
        }
    }

    @FXML
    void btnFindMemberOnAction() {
        String memberId = getTrimmedText(txtMemberId);
        if (memberId.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a Member ID.");
            return;
        }

        try {
            MemberDTO memberDto = libraryService.findMemberByCode(memberId);
            if (memberDto == null) {
                setLabelText(lblMemberDetails, "Member not found.");
                AlertHelper.showWarning(NOT_FOUND_TITLE, "No member found with ID: " + memberId);
                return;
            }
            setLabelText(lblMemberDetails, memberDto.getFullName() + " (" + memberDto.getEmail() + ")");
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to find member: " + e.getMessage());
        }
    }

    @FXML
    void btnFindBookOnAction() {
        String bookId = getTrimmedText(txtBookId);
        if (bookId.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a Book ID / ISBN.");
            return;
        }

        try {
            BookDTO bookDto = libraryService.findBookByCode(bookId);
            if (bookDto == null) {
                setLabelText(lblBookDetails, "Book not found.");
                AlertHelper.showWarning(NOT_FOUND_TITLE, "No book found with ID: " + bookId);
                return;
            }
            setLabelText(lblBookDetails, bookDto.getTitle() + " — Available: " + bookDto.getAvailableQuantity());
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to find book: " + e.getMessage());
        }
    }

    @FXML
    void btnIssueBookOnAction() {
        String memberId = getTrimmedText(txtMemberId);
        String bookId = getTrimmedText(txtBookId);
        LocalDate dueDate = dpDueDate != null ? dpDueDate.getValue() : null;
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (memberId.isEmpty() || bookId.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter both Member ID and Book ID.");
            return;
        }

        if (dueDate == null) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please select a valid due date.");
            return;
        }

        if (!dueDate.isAfter(today)) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Due date must be later than today (" + today + ").");
            return;
        }

        if (dueDate.isAfter(today.plusDays(MAX_LOAN_DAYS))) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Due date cannot exceed " + MAX_LOAN_DAYS + " days from today.");
            return;
        }

        try {
            libraryService.issueBook(memberId, bookId, today, dueDate);
            AlertHelper.showInfo("Success", "Book issued successfully.");
            clearForm();
        } catch (IllegalStateException | IllegalArgumentException e) {
            AlertHelper.showWarning("Issue Notice", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to issue book: " + e.getMessage());
        }
    }

    @FXML
    void btnClearIssueOnAction() {
        clearForm();
    }

    private void clearForm() {
        if (txtMemberId != null) {
            txtMemberId.clear();
        }
        if (txtBookId != null) {
            txtBookId.clear();
        }
        setLabelText(lblMemberDetails, "No member selected");
        setLabelText(lblBookDetails, "No book selected");
        if (dpDueDate != null) {
            dpDueDate.setValue(LocalDate.now(ZoneId.systemDefault()).plusDays(DEFAULT_LOAN_DAYS));
        }
        if (txtMemberId != null) {
            txtMemberId.requestFocus();
        }
    }

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private String getTrimmedText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }
}