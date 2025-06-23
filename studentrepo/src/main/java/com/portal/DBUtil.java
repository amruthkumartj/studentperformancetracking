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
            // Retrieve the complete JDBC URL from Railway's environment variables
            String jdbcUrl = System.getenv("MYSQL_URL");
            String DB_USER = System.getenv("MYSQL_USER");
            String DB_PASSWORD = System.getenv("MYSQL_PASSWORD");

            // --- IMPORTANT: Validate if environment variables are set ---
            if (jdbcUrl == null || DB_USER == null || DB_PASSWORD == null || jdbcUrl.isEmpty()) {
                System.err.println("🚫 Error: One or more Railway MySQL environment variables (MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD) are not set or MYSQL_URL is empty.");
                System.err.println("Ensure your application is in the same Railway project as your MySQL DB and variables are injected.");

                // --- FALLBACK FOR LOCAL DEVELOPMENT ---
                String localDbHost = "localhost";
                String localDbPort = "3306";
                String localDbName = "stud"; // CHANGE THIS to your local database name
                String localDbUser = "root";               // CHANGE THIS to your local MySQL username
                String localDbPassword = "root";           // CHANGE THIS to your local MySQL password

                jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    localDbHost, localDbPort, localDbName
                );
                DB_USER = localDbUser;
                DB_PASSWORD = localDbPassword;

                System.out.println("⚠️ Falling back to Local DB Configuration. If deployed on Railway, check variable injection.");
            } else {
                // --- FIX: Ensure the JDBC URL has the "jdbc:" prefix ---
                if (!jdbcUrl.startsWith("jdbc:mysql://")) {
                    jdbcUrl = "jdbc:" + jdbcUrl;
                    System.out.println("Fixed MYSQL_URL by adding 'jdbc:' prefix.");
                }
                System.out.println("Using Production DB Configuration (Railway variables).");
            }

            System.out.println("Attempting to connect to database URL: " + jdbcUrl); // Debugging
            conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            System.out.println("✅ Data base connected successfully to Railway MySQL!");
        } catch (SQLException e) {
            System.err.println("🚫 Failed to connect to DB: " + e.getMessage());
            System.err.println("Attempted URL: " + (System.getenv("MYSQL_URL") != null ? System.getenv("MYSQL_URL") : "Local Fallback URL"));
            System.err.println("Attempted User: " + (System.getenv("MYSQL_USER") != null ? System.getenv("MYSQL_USER") : "Local Fallback User"));
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