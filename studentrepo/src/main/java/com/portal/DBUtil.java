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
            // Railway can provide a single variable named MYSQL_URL which contains the full connection string.
            String jdbcUrl = System.getenv("MYSQL_URL");
            String DB_USER = System.getenv("MYSQL_USER");         // Still need user and password explicitly if not in URL
            String DB_PASSWORD = System.getenv("MYSQL_PASSWORD"); // Railway also provides MYSQL_ROOT_PASSWORD

            // --- IMPORTANT: Validate if environment variables are set ---
            // If MYSQL_URL is not set, we'll assume it's a local development environment
            // Or, if it's Railway, it means the variable wasn't injected correctly.
            if (jdbcUrl == null || DB_USER == null || DB_PASSWORD == null) {
                System.err.println("🚫 Error: One or more Railway MySQL environment variables (MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD) are not set.");
                System.err.println("Ensure your application is in the same Railway project as your MySQL DB and variables are injected.");

                // --- FALLBACK FOR LOCAL DEVELOPMENT ---
                // If these environment variables are not set (e.g., when running locally in Eclipse),
                // fall back to local hardcoded values.
                // IMPORTANT: Change these to your LOCAL MySQL credentials!
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
                // If you want to strictly fail on Railway if variables aren't there, remove this fallback.
                // For development, it's convenient.
            } else {
                System.out.println("Using Production DB Configuration (Railway variables)."); // For debugging
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