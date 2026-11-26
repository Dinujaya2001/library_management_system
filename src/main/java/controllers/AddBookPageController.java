package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.Book;
import service.LibraryService;

import java.sql.SQLException;
import java.time.Year;
import java.time.ZoneId;

public class AddBookPageController {

    private static final String VALIDATION_ERROR = "Validation Error";

    private final LibraryService libraryService = new LibraryService();

    @FXML
    private TextField txtBookId;

    @FXML
    private TextField txtBookTitle;

    @FXML
    private TextField txtAuthor;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private TextField txtPublishedYear;

    @FXML
    private TextField txtQuantity;

    @FXML
    public void initialize() {
        if (cmbCategory != null && cmbCategory.getItems().isEmpty()) {
            cmbCategory.getItems().addAll(
                    "Software Engineering",
                    "Computer Science",
                    "Fiction",
                    "Science",
                    "History",
                    "Mathematics",
                    "General"
            );
        }
    }

    @FXML
    void btnAddBookOnAction() {
        String bookCode = txtBookId != null && txtBookId.getText() != null ? txtBookId.getText().trim() : "";
        String title = txtBookTitle != null && txtBookTitle.getText() != null ? txtBookTitle.getText().trim() : "";
        String author = txtAuthor != null && txtAuthor.getText() != null ? txtAuthor.getText().trim() : "";
        String category = cmbCategory != null ? cmbCategory.getValue() : "";
        String yearText = txtPublishedYear != null && txtPublishedYear.getText() != null ? txtPublishedYear.getText().trim() : "";
        String quantityText = txtQuantity != null && txtQuantity.getText() != null ? txtQuantity.getText().trim() : "";

        if (bookCode.isEmpty() || title.isEmpty() || author.isEmpty()
                || category == null || category.isBlank()
                || yearText.isEmpty() || quantityText.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please fill in all required book details.");
            return;
        }

        int publishedYear;
        int quantity;
        int currentYear = Year.now(ZoneId.systemDefault()).getValue();

        try {
            publishedYear = Integer.parseInt(yearText);
            if (publishedYear < 1000 || publishedYear > currentYear) {
                AlertHelper.showWarning(VALIDATION_ERROR,
                        "Please enter a valid published year between 1000 and " + currentYear + ".");
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Published year must be a valid number.");
            return;
        }

        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                AlertHelper.showWarning(VALIDATION_ERROR, "Quantity must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Quantity must be a valid whole number.");
            return;
        }

        Book book = new Book(bookCode, title, author, category.trim(), publishedYear, quantity);

        try {
            libraryService.addBook(book);
            AlertHelper.showInfo("Success", "Book added successfully.");
            clearFields();
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning(VALIDATION_ERROR, e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to save book: " + e.getMessage());
        }
    }

    @FXML
    void btnClearBookOnAction() {
        clearFields();
    }

    private void clearFields() {
        if (txtBookId != null) {
            txtBookId.clear();
        }
        if (txtBookTitle != null) {
            txtBookTitle.clear();
        }
        if (txtAuthor != null) {
            txtAuthor.clear();
        }
        if (cmbCategory != null) {
            cmbCategory.getSelectionModel().clearSelection();
            cmbCategory.setValue(null);
        }
        if (txtPublishedYear != null) {
            txtPublishedYear.clear();
        }
        if (txtQuantity != null) {
            txtQuantity.clear();
        }
        if (txtBookId != null) {
            txtBookId.requestFocus();
        }
    }
}