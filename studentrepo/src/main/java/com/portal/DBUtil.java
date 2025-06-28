package com.portal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER);
            System.out.println("✅ MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("🚫 FATAL: MySQL JDBC Driver not found! Check your pom.xml.");
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    public static Connection getConnection() {
        Connection conn = null;
        String finalJdbcUrl = null;
        String dbUser = null;
        String dbPassword = null;

        try {
            // --- The Correct Production Approach: Build URL from individual parts ---
            String dbHost = System.getenv("MYSQLHOST");
            String dbPort = System.getenv("MYSQLPORT");
            String dbName = System.getenv("MYSQL_DATABASE");
            dbUser = System.getenv("MYSQL_USER");
            dbPassword = System.getenv("MYSQL_PASSWORD");

            // Check if we are in the Railway environment
            if (dbHost != null && dbPort != null && dbName != null && dbUser != null && dbPassword != null) {
                
                System.out.println("✅ Found Railway environment variables. Using them to build a clean JDBC URL.");
                
                finalJdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        dbHost, dbPort, dbName);

            } else {
                
                // --- Fallback for Local Development ONLY ---
                System.err.println("⚠️ Could not find Railway variables (MYSQLHOST, etc). Falling back to LOCAL DB Configuration.");
                
                String localDbHost = "localhost";
                String localDbPort = "3306";
                String localDbName = "stud"; 
                String localDbUser = "root";     
                String localDbPassword = "root"; 

                finalJdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        localDbHost, localDbPort, localDbName);
                dbUser = localDbUser;
                dbPassword = localDbPassword;
            }

            // --- Attempt the connection ---
            System.out.println("   - Attempting to connect to: " + finalJdbcUrl);
            System.out.println("   - User: " + dbUser);
            
            conn = DriverManager.getConnection(finalJdbcUrl, dbUser, dbPassword);
            
            System.out.println("✅✅✅ Database connection SUCCESSFUL!");

        } catch (SQLException e) {
            System.err.println("🚫🚫🚫 DATABASE CONNECTION FAILED!");
            System.err.println("   - Final URL Attempted: " + finalJdbcUrl);
            System.err.println("   - SQL State: " + e.getSQLState());
            System.err.println("   - Error Code: " + e.getErrorCode());
            System.err.println("   - Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database connection failed", e);
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}