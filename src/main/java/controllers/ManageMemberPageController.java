package controllers;

import dto.MemberDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import service.LibraryService;

import java.sql.SQLException;
import java.util.List;

public class ManageMemberPageController {

    private static final String DATABASE_ERROR = "Database Error";
    private static final String VALIDATION_ERROR = "Validation Error";

    private final LibraryService libraryService = new LibraryService();
    private final ObservableList<MemberDTO> memberList = FXCollections.observableArrayList();

    @FXML
    private TextField txtSearchMember;

    @FXML
    private TextField txtMemberId;

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtAddress;

    @FXML
    private TableView<MemberDTO> tblMembers;

    @FXML
    private TableColumn<MemberDTO, String> colMemberId;

    @FXML
    private TableColumn<MemberDTO, String> colFullName;

    @FXML
    private TableColumn<MemberDTO, String> colEmail;

    @FXML
    private TableColumn<MemberDTO, String> colPhone;

    @FXML
    private TableColumn<MemberDTO, String> colAddress;

    @FXML
    public void initialize() {
        bindColumn(colMemberId, "memberCode");
        bindColumn(colFullName, "fullName");
        bindColumn(colEmail, "email");
        bindColumn(colPhone, "phone");
        bindColumn(colAddress, "address");

        if (tblMembers != null) {
            tblMembers.setItems(memberList);
            tblMembers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> populateFields(newVal));
        }

        if (txtMemberId != null) {
            txtMemberId.setEditable(false);
        }

        loadMembers("");
    }

    private <T> void bindColumn(TableColumn<MemberDTO, T> column, String property) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(property));
        }
    }

    private void populateFields(MemberDTO member) {
        if (member == null) {
            clearForm();
            return;
        }
        setText(txtMemberId, member.getMemberCode());
        setText(txtFullName, member.getFullName());
        setText(txtEmail, member.getEmail());
        setText(txtPhone, member.getPhone());
        setText(txtAddress, member.getAddress());
    }

    private void loadMembers(String keyword) {
        try {
            List<MemberDTO> members = keyword.isEmpty()
                    ? libraryService.getAllMembers()
                    : libraryService.searchMembers(keyword);
            memberList.setAll(members);
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to load members: " + e.getMessage());
        }
    }

    @FXML
    void btnSearchMemberOnAction() {
        loadMembers(getTrimmedText(txtSearchMember));
    }

    @FXML
    void btnResetSelectionOnAction() {
        if (txtSearchMember != null) {
            txtSearchMember.clear();
        }
        if (tblMembers != null) {
            tblMembers.getSelectionModel().clearSelection();
        }
        clearForm();
        loadMembers("");
    }

    @FXML
    void btnUpdateMemberOnAction() {
        String memberId = getTrimmedText(txtMemberId);
        String fullName = getTrimmedText(txtFullName);
        String email = getTrimmedText(txtEmail);
        String phone = getTrimmedText(txtPhone);
        String address = getTrimmedText(txtAddress);

        if (memberId.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please select a member from the table to update.");
            return;
        }
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "All member fields are required.");
            return;
        }

        try {
            MemberDTO updatedDto = new MemberDTO(memberId, fullName, email, phone, address);
            libraryService.updateMember(updatedDto);
            AlertHelper.showInfo("Success", "Member updated successfully.");
            btnResetSelectionOnAction();
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to update member: " + e.getMessage());
        }
    }

    @FXML
    void btnDeleteMemberOnAction() {
        String memberId = getTrimmedText(txtMemberId);
        if (memberId.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please select a member to delete.");
            return;
        }

        if (!AlertHelper.showConfirmation("Confirm Delete", "Are you sure you want to delete member " + memberId + "?")) {
            return;
        }

        try {
            libraryService.deleteMember(memberId);
            AlertHelper.showInfo("Deleted", "Member removed successfully.");
            btnResetSelectionOnAction();
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Cannot delete member (active or historical borrow records exist): " + e.getMessage());
        }
    }

    private void clearForm() {
        TextField[] fields = {txtMemberId, txtFullName, txtEmail, txtPhone, txtAddress};
        for (TextField field : fields) {
            if (field != null) {
                field.clear();
            }
        }
    }

    private void setText(TextField field, String value) {
        if (field != null) {
            field.setText(value != null ? value : "");
        }
    }

    private String getTrimmedText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }
}