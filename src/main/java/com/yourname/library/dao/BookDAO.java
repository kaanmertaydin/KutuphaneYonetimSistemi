package com.yourname.library.dao;

import com.yourname.library.model.Book;
import com.yourname.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {



    public void addRating(int userId, int bookId, int rating) {
        String sql = "INSERT INTO book_ratings (user_id, book_id, rating) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.setInt(3, rating);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Book getBookById(int id) {
        String sql = "SELECT id, title, author, category, isbn, publisher, publish_year, status, " +
                "COALESCE((SELECT AVG(rating) FROM book_ratings WHERE book_id = books.id), 0) AS average_rating " +
                "FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createBookFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Book getBookByTitle(String title) {
        String sql = "SELECT id, title, author, category, isbn, publisher, publish_year, status, " +
                "COALESCE((SELECT AVG(rating) FROM book_ratings WHERE book_id = books.id), 0) AS average_rating " +
                "FROM books WHERE title = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createBookFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, category, isbn, publisher, publish_year, status, " +
                "COALESCE((SELECT AVG(rating) FROM book_ratings WHERE book_id = books.id), 0) AS average_rating " +
                "FROM books";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(createBookFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }


    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (title, author, category, isbn, publisher, publish_year, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setString(4, book.getIsbn());
            ps.setString(5, book.getPublisher());
            ps.setInt(6, book.getPublishYear());
            ps.setString(7, book.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, category = ?, isbn = ?, publisher = ?, publish_year = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setString(4, book.getIsbn());
            ps.setString(5, book.getPublisher());
            ps.setInt(6, book.getPublishYear());
            ps.setString(7, book.getStatus());
            ps.setInt(8, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean updateBookStatus(int bookId, String newStatus) {
        String sql = "UPDATE books SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Book createBookFromResultSet(ResultSet rs) throws SQLException {
        String averageRating = rs.getString("average_rating");
        String rating = averageRating != null ? String.format("%.1f", Double.parseDouble(averageRating)) : "Puan Yok";

        String status = rs.getString("status");

        Book book = new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                status,
                rating
        );

        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublishYear(rs.getInt("publish_year"));

        return book;
    }


    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, category, isbn, publisher, publish_year, status, " +
                "COALESCE((SELECT AVG(rating) FROM book_ratings WHERE book_id = books.id), 0) AS average_rating " +
                "FROM books WHERE title LIKE ? OR author LIKE ? OR category LIKE ? OR isbn LIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword.trim() + "%";
            for(int i=1; i<=4; i++) ps.setString(i, searchKeyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(createBookFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
}