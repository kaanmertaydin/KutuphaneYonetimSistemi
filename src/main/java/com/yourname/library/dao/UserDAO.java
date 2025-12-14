package com.yourname.library.dao;

import com.yourname.library.model.AbstractUser;
import com.yourname.library.model.Admin;
import com.yourname.library.model.Student;
import com.yourname.library.model.Staff;
import com.yourname.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private AbstractUser createUserFromResultSet(ResultSet rs) throws SQLException {
        int userId = rs.getInt("id");
        String fName = rs.getString("first_name");
        String lName = rs.getString("last_name");
        String tcNo = rs.getString("tc_no");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String userType = rs.getString("user_type");
        String number = rs.getString("number");
        String phone = rs.getString("phone");


        return com.yourname.library.pattern.factory.UserFactory.createUser(
                userType, userId, fName, lName, tcNo, email, password, number, phone
        );
    }

    public AbstractUser getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return createUserFromResultSet(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public AbstractUser getUserByCredential(String input) {
        String sql = "SELECT * FROM users WHERE email=? OR tc_no=? OR number=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ps.setString(2, input);
            ps.setString(3, input);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return createUserFromResultSet(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public AbstractUser getUserByEmail(String email) {
        return getUserByCredential(email);
    }

    public boolean updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET user_type = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEmailTaken(String email) { return checkExistence("email", email); }
    public boolean isTcNoTaken(String tcNo) { return checkExistence("tc_no", tcNo); }
    public boolean isPhoneTaken(String phone) { return checkExistence("phone", phone); }
    public boolean isNumberTaken(String number) { return checkExistence("number", number); }

    private boolean checkExistence(String column, String value) {
        String sql = "SELECT id FROM users WHERE " + column + " = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean addUser(AbstractUser user) {
        String sql = "INSERT INTO users (first_name, last_name, tc_no, email, password, user_type, number, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getTcNo());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());


            String type = "Student";
            if (user instanceof Staff) type = "Staff";
            else if (user instanceof Admin) type = "Admin";
            ps.setString(6, type);

            ps.setString(7, user.getNumber());
            ps.setString(8, user.getPhone());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateUser(AbstractUser user) {
        String sql = "UPDATE users SET first_name=?, last_name=?, tc_no=?, email=?, password=?, user_type=?, number=?, phone=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getTcNo());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());

            String type = "Student";
            if (user instanceof Staff) type = "Staff";
            else if (user instanceof Admin) type = "Admin";
            ps.setString(6, type);

            ps.setString(7, user.getNumber());
            ps.setString(8, user.getPhone());
            ps.setInt(9, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<AbstractUser> getAllUsers() {
        List<AbstractUser> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(createUserFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public List<AbstractUser> searchUsers(String keyword) {
        List<AbstractUser> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR number LIKE ? OR tc_no LIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword.trim() + "%";
            for(int i=1; i<=5; i++) ps.setString(i, searchKeyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }
}