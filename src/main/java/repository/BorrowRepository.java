package repository;

import config.DatabaseConnection;
import model.BorrowRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRepository {

    public boolean saveIssue(BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (member_code, member_name, book_code, book_title, issue_date, due_date, status) VALUES (?, ?, ?, ?, ?, ?, 'Borrowed')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.getMemberCode());
            stmt.setString(2, record.getMemberName());
            stmt.setString(3, record.getBookCode());
            stmt.setString(4, record.getBookTitle());
            stmt.setDate(5, Date.valueOf(record.getIssueDate()));
            stmt.setDate(6, Date.valueOf(record.getDueDate()));
            return stmt.executeUpdate() > 0;
        }
    }

    public List<BorrowRecord> findActiveBorrowedRecords() throws SQLException {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records WHERE status IN ('Borrowed', 'Overdue') ORDER BY due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean markReturned(int recordId, LocalDate returnDate, String status) throws SQLException {
        String sql = "UPDATE borrow_records SET return_date = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(returnDate));
            stmt.setString(2, status);
            stmt.setInt(3, recordId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<BorrowRecord> findAllHistory() throws SQLException {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countCurrentlyBorrowed() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE return_date IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countOverdue() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE return_date IS NULL AND due_date < CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setMemberCode(rs.getString("member_code"));
        record.setMemberName(rs.getString("member_name"));
        record.setBookCode(rs.getString("book_code"));
        record.setBookTitle(rs.getString("book_title"));

        Date issue = rs.getDate("issue_date");
        Date due = rs.getDate("due_date");
        Date ret = rs.getDate("return_date");

        record.setIssueDate(issue != null ? issue.toLocalDate() : null);
        record.setDueDate(due != null ? due.toLocalDate() : null);
        record.setReturnDate(ret != null ? ret.toLocalDate() : null);

        String dbStatus = rs.getString("status");
        if (record.getReturnDate() == null && record.getDueDate() != null && LocalDate.now().isAfter(record.getDueDate())) {
            record.setStatus("Overdue");
        } else {
            record.setStatus(dbStatus);
        }
        return record;
    }
}