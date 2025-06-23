package com.portal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            // Load JDBC driver once when the class loads
            Class.forName(DRIVER);
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("🚫 Error: MySQL JDBC Driver not found! Please check your pom.xml and dependencies.");
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Retrieve connection details from Railway's environment variables
            String DB_HOST = System.getenv("MYSQL_HOST");
            String DB_PORT = System.getenv("MYSQL_PORT");
            String DB_USER = System.getenv("MYSQL_USER");
            String DB_PASSWORD = System.getenv("MYSQL_PASSWORD");
            String DB_NAME = System.getenv("MYSQL_DATABASE"); // Railway uses MYSQL_DATABASE

            // --- IMPORTANT: Validate if environment variables are set ---
            if (DB_HOST == null || DB_PORT == null || DB_USER == null || DB_PASSWORD == null || DB_NAME == null) {
                System.err.println("🚫 Error: One or more Railway MySQL environment variables are not set.");
                System.err.println("Ensure your application is in the same Railway project as your MySQL DB.");
                // For local development, you could add a fallback to your local DB here,
                // but for deployment, these must be set by Railway.
                throw new SQLException("Required database environment variables are missing for Railway deployment.");
            }

            // Construct the JDBC URL using environment variables
            // Added allowPublicKeyRetrieval=true for compatibility with newer MySQL servers
            String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                DB_HOST, DB_PORT, DB_NAME
            );

            conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            System.out.println("✅ Data base connected successfully to Railway MySQL!");
        } catch (SQLException e) {
            System.err.println("🚫 Failed to connect to DB: " + e.getMessage());
            System.err.println("Host: " + System.getenv("MYSQL_HOST") + ", Port: " + System.getenv("MYSQL_PORT") + ", DB: " + System.getenv("MYSQL_DATABASE") + ", User: " + System.getenv("MYSQL_USER"));
            e.printStackTrace();
            throw new RuntimeException("Database connection failed", e);
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}