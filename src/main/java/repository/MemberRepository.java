package repository;

import config.DatabaseConnection;
import model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberRepository {

    public int getTotalMembersCount() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM members";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    public void save(Member member) throws SQLException {
        String sql = "INSERT INTO members (member_code, full_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getMemberCode());
            stmt.setString(2, member.getFullName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getAddress());
            stmt.executeUpdate();
        }
    }

    public Member findByCode(String memberCode) throws SQLException {
        String sql = "SELECT member_code, full_name, email, phone, address FROM members WHERE member_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Member> findAll() throws SQLException {
        String sql = "SELECT member_code, full_name, email, phone, address FROM members ORDER BY full_name ASC";
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        }
        return members;
    }

    public List<Member> search(String keyword) throws SQLException {
        String sql = "SELECT member_code, full_name, email, phone, address FROM members WHERE LOWER(member_code) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? ORDER BY full_name ASC";
        String pattern = "%" + keyword.toLowerCase() + "%";
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
        }
        return members;
    }

    public void update(Member member) throws SQLException {
        String sql = "UPDATE members SET full_name = ?, email = ?, phone = ?, address = ? WHERE member_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getFullName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getAddress());
            stmt.setString(5, member.getMemberCode());
            stmt.executeUpdate();
        }
    }

    public void deleteByCode(String memberCode) throws SQLException {
        String sql = "DELETE FROM members WHERE member_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberCode);
            stmt.executeUpdate();
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
                rs.getString("member_code"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address")
        );
    }
}