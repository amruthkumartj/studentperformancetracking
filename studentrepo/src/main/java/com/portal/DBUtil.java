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
            System.out.println("✅ MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("🚫 FATAL: MySQL JDBC Driver not found! Please check your pom.xml and dependencies.");
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    public static Connection getConnection() {
        Connection conn = null;
        String jdbcUrl = null;
        String dbUser = null;
        String dbPassword = null;
        
        try {
            // Step 1: Attempt to retrieve database credentials from Railway's environment variables
            String railwayUrl = System.getenv("MYSQL_URL");
            String railwayUser = System.getenv("MYSQL_USER");
            String railwayPassword = System.getenv("MYSQL_PASSWORD");

            // Step 2: Check if the Railway environment variables are present and valid
            if (railwayUrl != null && !railwayUrl.isEmpty() && railwayUser != null && railwayPassword != null) {
                
                System.out.println("✅ Found Railway environment variables. Using Production DB Configuration.");
                jdbcUrl = railwayUrl;
                dbUser = railwayUser;
                dbPassword = railwayPassword;

                // --- IMPORTANT FIX: Ensure the JDBC URL is correctly formatted for the driver ---
                if (!jdbcUrl.startsWith("jdbc:mysql://")) {
                    // Railway's URL is typically "mysql://..." but JDBC requires "jdbc:mysql://"
                    jdbcUrl = "jdbc:" + jdbcUrl;
                    System.out.println("   - Fixed MYSQL_URL by adding 'jdbc:' prefix.");
                }

                // --- IMPORTANT FIX: Append required JDBC parameters if they are missing ---
                if (!jdbcUrl.contains("?")) {
                    // These parameters are good practice for compatibility and avoiding timezone errors
                    jdbcUrl += "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                    System.out.println("   - Appended standard JDBC parameters to the URL for compatibility.");
                }

            } else {
                
                // --- FALLBACK FOR LOCAL DEVELOPMENT ---
                System.err.println("⚠️ Could not find Railway variables. Falling back to LOCAL DB Configuration.");
                System.err.println("   - If deployed on Railway, this is an ERROR. Check variable injection.");
                
                String localDbHost = "localhost";
                String localDbPort = "3306";
                String localDbName = "stud"; // Your local database name
                String localDbUser = "root";     // Your local MySQL username
                String localDbPassword = "root"; // Your local MySQL password

                jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        localDbHost, localDbPort, localDbName);
                dbUser = localDbUser;
                dbPassword = localDbPassword;
            }

            // Step 3: Attempt the database connection with the determined credentials
            System.out.println("   - Attempting to connect to: " + jdbcUrl);
            conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
            System.out.println("✅✅✅ Database connection SUCCESSFUL!");

        } catch (SQLException e) {
            System.err.println("🚫🚫🚫 DATABASE CONNECTION FAILED!");
            System.err.println("   - Final URL Attempted: " + jdbcUrl); // Log the exact URL that failed
            System.err.println("   - SQL State: " + e.getSQLState());
            System.err.println("   - Error Code: " + e.getErrorCode());
            System.err.println("   - Message: " + e.getMessage());
            e.printStackTrace();
            // This re-throws the exception so the calling code knows it failed.
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