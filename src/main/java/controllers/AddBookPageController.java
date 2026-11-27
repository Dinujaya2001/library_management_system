package controllers;

import dto.BookDTO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import service.LibraryService;

import java.sql.SQLException;
import java.time.Year;
import java.time.ZoneId;

public class AddBookPageController {

    private static final String VALIDATION_ERROR = "Validation Error";
    private static final int MIN_PUBLISHED_YEAR = 1450;

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
        String bookCode = getTrimmedText(txtBookId);
        String title = getTrimmedText(txtBookTitle);
        String author = getTrimmedText(txtAuthor);
        String category = cmbCategory != null && cmbCategory.getValue() != null ? cmbCategory.getValue().trim() : "";
        String yearText = getTrimmedText(txtPublishedYear);
        String quantityText = getTrimmedText(txtQuantity);

        if (bookCode.isEmpty() || title.isEmpty() || author.isEmpty()
                || category.isEmpty() || yearText.isEmpty() || quantityText.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "All book fields are required.");
            return;
        }

        int publishedYear = parsePublishedYear(yearText);
        if (publishedYear < 0) {
            return;
        }

        int quantity = parsePositiveQuantity(quantityText);
        if (quantity < 0) {
            return;
        }

        try {
            if (libraryService.findBookByCode(bookCode) != null) {
                AlertHelper.showWarning(VALIDATION_ERROR, "A book with ID '" + bookCode + "' already exists.");
                return;
            }

            BookDTO bookDto = new BookDTO(bookCode, title, author, category, publishedYear, quantity);
            libraryService.addBook(bookDto);
            AlertHelper.showInfo("Success", "Book '" + title + "' added successfully.");
            clearFields();
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning(VALIDATION_ERROR, e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Unable to save book: " + e.getMessage());
        }
    }

    private int parsePublishedYear(String yearText) {
        int currentYear = Year.now(ZoneId.systemDefault()).getValue();
        try {
            int year = Integer.parseInt(yearText);
            if (year < MIN_PUBLISHED_YEAR || year > currentYear) {
                AlertHelper.showWarning(VALIDATION_ERROR,
                        "Published year must be between " + MIN_PUBLISHED_YEAR + " and " + currentYear + ".");
                return -1;
            }
            return year;
        } catch (NumberFormatException e) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Published year must be a valid four-digit number.");
            return -1;
        }
    }

    private int parsePositiveQuantity(String quantityText) {
        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                AlertHelper.showWarning(VALIDATION_ERROR, "Quantity must be greater than zero.");
                return -1;
            }
            return quantity;
        } catch (NumberFormatException e) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Quantity must be a valid whole number.");
            return -1;
        }
    }

    @FXML
    void btnClearBookOnAction() {
        clearFields();
    }

    private void clearFields() {
        TextField[] fields = {txtBookId, txtBookTitle, txtAuthor, txtPublishedYear, txtQuantity};
        for (TextField field : fields) {
            if (field != null) {
                field.clear();
            }
        }
        if (cmbCategory != null) {
            cmbCategory.getSelectionModel().clearSelection();
            cmbCategory.setValue(null);
        }
        if (txtBookId != null) {
            txtBookId.requestFocus();
        }
    }

    private String getTrimmedText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }
}