package com.yourname.library.dao;

import com.yourname.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowLogDAO {

    /**
     * Kullanıcının ödünç aldığı ve henüz iade etmediği kitapları getirir.
     * Tablo adı: loans
     *
     * @param userId Kullanıcının ID'si.
     * @return String dizisi listesi: {Kitap Başlığı, Ödünç Tarihi, Son Teslim Tarihi}
     */
    public List<String[]> getBorrowedBooksByUser(int userId) {
        List<String[]> borrowedBooks = new ArrayList<>();



        String sql = "SELECT b.title, l.borrow_date, l.return_date FROM loans l " +
                "JOIN books b ON l.book_id = b.id " +
                "WHERE l.user_id = ? AND l.actual_return_date IS NULL";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    borrowedBooks.add(new String[]{
                            rs.getString("title"),
                            rs.getString("borrow_date"),
                            rs.getString("return_date")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return borrowedBooks;
    }

    /**
     * Ödünç alma kaydı ekler.
     * PDF Gereksinimi: Kitap ödünç verildiğinde bir iade tarihi belirlenmelidir.
     *
     * @param userId Kullanıcının ID'si.
     * @param bookId Kitabın ID'si.
     */
    public void addBorrowLog(int userId, int bookId) {

        String sql = "INSERT INTO loans (user_id, book_id, borrow_date, return_date, status) " +
                "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 'Active')";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kitabın iade kaydını günceller.
     *
     * @param userId Kullanıcının ID'si.
     * @param bookId Kitabın ID'si.
     */
    public void updateReturnLog(int userId, int bookId) {

        String sql = "UPDATE loans SET actual_return_date = NOW(), status = 'Returned' " +
                "WHERE user_id = ? AND book_id = ? AND actual_return_date IS NULL";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated == 0) {
                System.out.println("HATA: İade işlemi yapılamadı. Aktif bir ödünç kaydı bulunamadı.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}