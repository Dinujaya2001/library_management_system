package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Member;
import service.LibraryService;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class ManageMemberPageController {

    private static final String DATABASE_ERROR = "Database Error";
    private static final String VALIDATION_ERROR = "Validation Error";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-\\s]{7,15}$");

    private final LibraryService libraryService = new LibraryService();
    private final ObservableList<Member> memberList = FXCollections.observableArrayList();

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
    private TableView<Member> tblMembers;

    @FXML
    private TableColumn<Member, String> colMemberId;

    @FXML
    private TableColumn<Member, String> colFullName;

    @FXML
    private TableColumn<Member, String> colEmail;

    @FXML
    private TableColumn<Member, String> colPhone;

    @FXML
    private TableColumn<Member, String> colAddress;

    private Member selectedMember;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadMembers();

        if (tblMembers != null) {
            tblMembers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateFields(newSelection);
                }
            });
        }
    }

    private void setupTableColumns() {
        if (colMemberId != null) {
            colMemberId.setCellValueFactory(new PropertyValueFactory<>("memberCode"));
        }
        if (colFullName != null) {
            colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        }
        if (colEmail != null) {
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        }
        if (colPhone != null) {
            colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        }
        if (colAddress != null) {
            colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        }
        if (tblMembers != null) {
            tblMembers.setItems(memberList);
        }
    }

    private void loadMembers() {
        try {
            List<Member> members = libraryService.getAllMembers();
            memberList.setAll(members);
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to load members: " + e.getMessage());
        }
    }

    private void populateFields(Member member) {
        selectedMember = member;
        if (txtMemberId != null) {
            txtMemberId.setText(member.getMemberCode());
            txtMemberId.setEditable(false);
        }
        if (txtFullName != null) {
            txtFullName.setText(member.getFullName());
        }
        if (txtEmail != null) {
            txtEmail.setText(member.getEmail());
        }
        if (txtPhone != null) {
            txtPhone.setText(member.getPhone());
        }
        if (txtAddress != null) {
            txtAddress.setText(member.getAddress());
        }
    }

    private Member resolveSelectedMember() {
        if (selectedMember != null) {
            return selectedMember;
        }
        if (tblMembers != null) {
            return tblMembers.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    private String getTrimmedText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }

    private boolean isValidMemberInput(String fullName, String email, String phone, String address) {
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please fill in all required member details.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a valid email address.");
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            AlertHelper.showWarning(VALIDATION_ERROR, "Please enter a valid phone number.");
            return false;
        }
        return true;
    }

    @FXML
    void btnSearchMemberOnAction() {
        String keyword = getTrimmedText(txtSearchMember);
        try {
            List<Member> results = libraryService.searchMembers(keyword);
            memberList.setAll(results);
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to search members: " + e.getMessage());
        }
    }

    @FXML
    void btnUpdateMemberOnAction() {
        Member target = resolveSelectedMember();
        if (target == null) {
            AlertHelper.showWarning("No Selection", "Please select a member from the table to update.");
            return;
        }

        String fullName = getTrimmedText(txtFullName);
        String email = getTrimmedText(txtEmail);
        String phone = getTrimmedText(txtPhone);
        String address = getTrimmedText(txtAddress);

        if (!isValidMemberInput(fullName, email, phone, address)) {
            return;
        }

        Member updated = new Member(target.getMemberCode(), fullName, email, phone, address);

        try {
            libraryService.updateMember(updated);
            AlertHelper.showInfo("Success", "Member details updated successfully.");
            loadMembers();
            clearSelection();
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to update member: " + e.getMessage());
        }
    }

    @FXML
    void btnDeleteMemberOnAction() {
        Member target = resolveSelectedMember();
        if (target == null) {
            AlertHelper.showWarning("No Selection", "Please select a member from the table to delete.");
            return;
        }

        boolean confirmed = AlertHelper.showConfirmation("Confirm Delete",
                "Are you sure you want to delete member '" + target.getFullName() + "'?");
        if (!confirmed) {
            return;
        }

        try {
            libraryService.deleteMember(target.getMemberCode());
            AlertHelper.showInfo("Success", "Member deleted successfully.");
            loadMembers();
            clearSelection();
        } catch (SQLException e) {
            AlertHelper.showError(DATABASE_ERROR, "Failed to delete member: " + e.getMessage());
        }
    }

    @FXML
    void btnResetSelectionOnAction() {
        clearSelection();
        loadMembers();
    }

    private void clearTextFields() {
        TextField[] fields = {txtMemberId, txtFullName, txtEmail, txtPhone, txtAddress};
        for (TextField field : fields) {
            if (field != null) {
                field.clear();
            }
        }
    }

    private void clearSelection() {
        selectedMember = null;
        if (tblMembers != null) {
            tblMembers.getSelectionModel().clearSelection();
        }
        clearTextFields();
        if (txtMemberId != null) {
            txtMemberId.setEditable(true);
        }
    }
}