package repository;

import config.DatabaseConnection;
import model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookRepository {

    public int getTotalBooksCount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) AS total FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    public void save(Book book) throws SQLException {
        String sql = "INSERT INTO books (book_code, title, author, category, published_year, quantity, available_quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getBookCode());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCategory());
            stmt.setInt(5, book.getPublishedYear());
            stmt.setInt(6, book.getQuantity());
            stmt.setInt(7, book.getQuantity());
            stmt.executeUpdate();
        }
    }

    public Book findByCode(String bookCode) throws SQLException {
        String sql = "SELECT book_code, title, author, category, published_year, quantity, available_quantity FROM books WHERE book_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Book book = new Book(
                            rs.getString("book_code"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("category"),
                            rs.getInt("published_year"),
                            rs.getInt("quantity")
                    );
                    book.setAvailableQuantity(rs.getInt("available_quantity"));
                    return book;
                }
            }
        }
        return null;
    }

    public void decrementAvailableQuantity(String bookCode) throws SQLException {
        String sql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_code = ? AND available_quantity > 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookCode);
            stmt.executeUpdate();
        }
    }

    public void incrementAvailableQuantity(String bookCode) throws SQLException {
        String sql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_code = ? AND available_quantity < quantity";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookCode);
            stmt.executeUpdate();
        }
    }
}