package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.BorrowRecord;
import service.LibraryService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReturnBookPageController {

    private static final String DATABASE_ERROR = "Database Error";
    private static final String NO_SELECTION_TITLE = "No Selection";
    private static final long DAILY_FINE_RATE = 50L;

    private final LibraryService libraryService = new LibraryService();
    private final ObservableList<BorrowRecord> activeRecords = FXCollections.observableArrayList();

    @FXML
    private TextField txtSearchRecord;

    @FXML
    private Label lblSelectedInfo;

    @FXML
    private Label lblFineAmount;

    @FXML
    private TableView<BorrowRecord> tblActiveBorrows;

    @FXML
    private TableColumn<BorrowRecord, Integer> colRecordId;

    @FXML
    private TableColumn<BorrowRecord, String> colMemberId;

    @FXML
    private TableColumn<BorrowRecord, String> colBookId;

    @FXML
    private TableColumn<BorrowRecord, LocalDate> colIssueDate;

    @FXML
    private TableColumn<BorrowRecord, LocalDate> colDueDate;

    @FXML
    private TableColumn<BorrowRecord, String> colStatus;

    private BorrowRecord selectedRecord;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadActiveRecords("");

        if (tblActiveBorrows != null) {
            tblActiveBorrows.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                selectedRecord = newVal;
                updateSelectionSummary(newVal);
            });
        }
    }

    private <T> void bindColumn(TableColumn<BorrowRecord, T> column, String propertyName) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
    }

    private void setupTableColumns() {
        bindColumn(colRecordId, "id");
        bindColumn(colMemberId, "memberCode");
        bindColumn(colBookId, "bookCode");
        bindColumn(colIssueDate, "issueDate");
        bindColumn(colDueDate, "dueDate");
        bindColumn(colStatus, "status");

        if (tblActiveBorrows != null) {
            tblActiveBorrows.setItems(activeRecords);
        }
    }

    private void loadActiveRecords(String keyword) {
        try {
            List<BorrowRecord> records = keyword.isEmpty()
                    ? libraryService.getActiveBorrowRecords()
                    : libraryService.searchActiveBorrowRecords(keyword);
            activeRecords.setAll(records);
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to load active borrow records: " + e.getMessage());
        }
    }

    private void updateSelectionSummary(BorrowRecord borrowRecord) {
        if (borrowRecord == null) {
            if (lblSelectedInfo != null) {
                lblSelectedInfo.setText("No borrow record selected");
            }
            if (lblFineAmount != null) {
                lblFineAmount.setText("Fine: LKR 0.00");
            }
            return;
        }

        if (lblSelectedInfo != null) {
            lblSelectedInfo.setText("Member: " + borrowRecord.getMemberCode() + " | Book: " + borrowRecord.getBookCode());
        }

        long fine = calculateFine(borrowRecord.getDueDate());
        if (lblFineAmount != null) {
            lblFineAmount.setText("Fine: LKR " + fine + ".00");
        }
    }

    private long calculateFine(LocalDate dueDate) {
        if (dueDate == null) {
            return 0L;
        }
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        if (!today.isAfter(dueDate)) {
            return 0L;
        }
        long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
        return overdueDays * DAILY_FINE_RATE;
    }

    private BorrowRecord resolveSelectedRecord() {
        if (selectedRecord != null) {
            return selectedRecord;
        }
        if (tblActiveBorrows != null) {
            return tblActiveBorrows.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    @FXML
    void btnSearchReturnOnAction() {
        loadActiveRecords(getTrimmedText(txtSearchRecord));
    }

    @FXML
    void btnRefreshReturnOnAction() {
        if (txtSearchRecord != null) {
            txtSearchRecord.clear();
        }
        selectedRecord = null;
        if (tblActiveBorrows != null) {
            tblActiveBorrows.getSelectionModel().clearSelection();
        }
        updateSelectionSummary(null);
        loadActiveRecords("");
    }

    @FXML
    void btnReturnBookOnAction() {
        BorrowRecord target = resolveSelectedRecord();
        if (target == null) {
            AlertHelper.showWarning(NO_SELECTION_TITLE, "Please select a borrowed record to return.");
            return;
        }

        long fine = calculateFine(target.getDueDate());
        String confirmMessage = fine > 0
                ? "Book is overdue. Estimated fine: LKR " + fine + ".00. Confirm return?"
                : "Confirm returning this book?";

        if (!AlertHelper.showConfirmation("Confirm Return", confirmMessage)) {
            return;
        }

        try {
            libraryService.returnBook(target.getId(), LocalDate.now(ZoneId.systemDefault()));
            AlertHelper.showInfo("Success", "Book returned successfully.");
            btnRefreshReturnOnAction();
        } catch (IllegalStateException | IllegalArgumentException e) {
            AlertHelper.showWarning("Return Notice", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to return book: " + e.getMessage());
        }
    }

    private String getTrimmedText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }
}