package com.yourname.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;


    private final String url = "jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=utf8";
    private final String username = "root";
    private final String password = "";


    private DatabaseConnection() {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Veritabanına başarıyla bağlanıldı.");
        } catch (ClassNotFoundException e) {
            System.out.println("HATA: MySQL JDBC sürücüsü bulunamadı. Lütfen kütüphaneyi (JAR) eklediğinizden emin olun.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("HATA: Veritabanına bağlanılamadı. XAMPP/MySQL açık mı?");
            System.out.println("Hata Detayı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
}