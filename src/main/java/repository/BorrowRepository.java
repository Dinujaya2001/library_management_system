package repository;

import config.DatabaseConnection;
import model.BorrowRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRepository {

    private static final String BASE_BORROW_SELECT =
            "SELECT br.id, br.member_code, m.full_name AS member_name, br.book_code, b.title AS book_title, " +
                    "br.issue_date, br.due_date, br.return_date, br.status " +
                    "FROM borrow_records br " +
                    "LEFT JOIN members m ON br.member_code = m.member_code " +
                    "LEFT JOIN books b ON br.book_code = b.book_code ";

    public int getCurrentlyBorrowedCount() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM borrow_records WHERE status IN ('ISSUED', 'OVERDUE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    public int getOverdueCount(LocalDate today) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM borrow_records WHERE return_date IS NULL AND due_date < ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, today);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public void issueBook(String memberCode, String bookCode, LocalDate issueDate, LocalDate dueDate)
            throws SQLException {
        String sql = "INSERT INTO borrow_records (member_code, book_code, issue_date, due_date, status) VALUES (?, ?, ?, ?, 'ISSUED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberCode);
            stmt.setString(2, bookCode);
            stmt.setObject(3, issueDate);
            stmt.setObject(4, dueDate);
            stmt.executeUpdate();
        }
    }

    public BorrowRecord findById(int recordId) throws SQLException {
        String sql = BASE_BORROW_SELECT + "WHERE br.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<BorrowRecord> findActiveRecords() throws SQLException {
        return executeRecordQuery(BASE_BORROW_SELECT + "WHERE br.return_date IS NULL ORDER BY br.due_date ASC");
    }

    public List<BorrowRecord> searchActiveRecords(String keyword) throws SQLException {
        String sql = BASE_BORROW_SELECT +
                "WHERE br.return_date IS NULL AND (CAST(br.id AS CHAR) LIKE ? OR LOWER(br.member_code) LIKE ? " +
                "OR LOWER(br.book_code) LIKE ? OR LOWER(COALESCE(m.full_name, '')) LIKE ? OR LOWER(COALESCE(b.title, '')) LIKE ?) " +
                "ORDER BY br.due_date ASC";
        String pattern = "%" + keyword.toLowerCase() + "%";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, pattern);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        }
        return records;
    }

    public void markAsReturned(int recordId, LocalDate returnDate) throws SQLException {
        String sql = "UPDATE borrow_records SET return_date = ?, status = 'RETURNED' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, returnDate);
            stmt.setInt(2, recordId);
            stmt.executeUpdate();
        }
    }

    public List<BorrowRecord> findAllHistory() throws SQLException {
        return executeRecordQuery(BASE_BORROW_SELECT + "ORDER BY br.issue_date DESC, br.id DESC");
    }

    private List<BorrowRecord> executeRecordQuery(String sql) throws SQLException {
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        }
        return records;
    }

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setId(rs.getInt("id"));
        borrowRecord.setMemberCode(rs.getString("member_code"));
        borrowRecord.setMemberName(rs.getString("member_name"));
        borrowRecord.setBookCode(rs.getString("book_code"));
        borrowRecord.setBookTitle(rs.getString("book_title"));
        borrowRecord.setIssueDate(rs.getObject("issue_date", LocalDate.class));
        borrowRecord.setDueDate(rs.getObject("due_date", LocalDate.class));
        borrowRecord.setReturnDate(rs.getObject("return_date", LocalDate.class));
        borrowRecord.setStatus(rs.getString("status"));
        return borrowRecord;
    }
}