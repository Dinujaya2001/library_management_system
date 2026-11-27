package controllers;

import dto.BorrowRecordDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import service.LibraryService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class HistoryPageController {

    private static final String DATABASE_ERROR = "Database Error";
    private static final String FILTER_ALL = "All";

    private final LibraryService libraryService = new LibraryService();
    private final ObservableList<BorrowRecordDTO> historyList = FXCollections.observableArrayList();

    @FXML
    private TextField txtSearchHistory;

    @FXML
    private ComboBox<String> cmbStatusFilter;

    @FXML
    private TableView<BorrowRecordDTO> tblHistory;

    @FXML
    private TableColumn<BorrowRecordDTO, Integer> colRecordId;

    @FXML
    private TableColumn<BorrowRecordDTO, String> colMemberId;

    @FXML
    private TableColumn<BorrowRecordDTO, String> colMemberName;

    @FXML
    private TableColumn<BorrowRecordDTO, String> colBookId;

    @FXML
    private TableColumn<BorrowRecordDTO, String> colBookTitle;

    @FXML
    private TableColumn<BorrowRecordDTO, LocalDate> colIssueDate;

    @FXML
    private TableColumn<BorrowRecordDTO, LocalDate> colDueDate;

    @FXML
    private TableColumn<BorrowRecordDTO, LocalDate> colReturnDate;

    @FXML
    private TableColumn<BorrowRecordDTO, String> colStatus;

    @FXML
    public void initialize() {
        setupFilterCombo();
        setupTableColumns();
        loadHistory(FILTER_ALL, "");
    }

    private void setupFilterCombo() {
        if (cmbStatusFilter != null) {
            if (cmbStatusFilter.getItems().isEmpty()) {
                cmbStatusFilter.getItems().addAll(FILTER_ALL, "ISSUED", "RETURNED", "OVERDUE");
            }
            if (cmbStatusFilter.getValue() == null) {
                cmbStatusFilter.setValue(FILTER_ALL);
            }
        }
    }

    private <T> void bindColumn(TableColumn<BorrowRecordDTO, T> column, String propertyName) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
    }

    private void setupTableColumns() {
        bindColumn(colRecordId, "id");
        bindColumn(colMemberId, "memberCode");
        bindColumn(colMemberName, "memberName");
        bindColumn(colBookId, "bookCode");
        bindColumn(colBookTitle, "bookTitle");
        bindColumn(colIssueDate, "issueDate");
        bindColumn(colDueDate, "dueDate");
        bindColumn(colReturnDate, "returnDate");
        bindColumn(colStatus, "status");

        if (tblHistory != null) {
            tblHistory.setItems(historyList);
        }
    }

    private void loadHistory(String status, String keyword) {
        try {
            List<BorrowRecordDTO> records = libraryService.getBorrowHistory(status, keyword);
            historyList.setAll(records);
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to load borrowing history: " + e.getMessage());
        }
    }

    @FXML
    void btnFilterHistoryOnAction() {
        String status = cmbStatusFilter != null && cmbStatusFilter.getValue() != null
                ? cmbStatusFilter.getValue()
                : FILTER_ALL;
        String keyword = getTrimmedText(txtSearchHistory);
        loadHistory(status, keyword);
    }

    @FXML
    void btnResetHistoryOnAction() {
        if (txtSearchHistory != null) {
            txtSearchHistory.clear();
        }
        if (cmbStatusFilter != null) {
            cmbStatusFilter.setValue(FILTER_ALL);
        }
        loadHistory(FILTER_ALL, "");
    }

    private String getTrimmedText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }
}