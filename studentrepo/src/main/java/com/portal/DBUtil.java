package com.portal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // --- Aiven MySQL Connection Details (from Environment Variables for Render) ---
    // These will be read from Render's environment variables.
    // Make sure these names match exactly what you configure on Render.
    private static final String AIVEN_HOST_ENV = "AIVEN_MYSQL_HOST";
    private static final String AIVEN_PORT_ENV = "AIVEN_MYSQL_PORT";
    private static final String AIVEN_DATABASE_ENV = "AIVEN_MYSQL_DATABASE"; // Should be "stud"
    private static final String AIVEN_USERNAME_ENV = "AIVEN_MYSQL_USERNAME";
    private static final String AIVEN_PASSWORD_ENV = "AIVEN_MYSQL_PASSWORD";

    // --- Local MySQL Connection Details (Fallback) ---
    // These are your local MySQL credentials.
    private static final String LOCAL_DB_HOST = "localhost";
    private static final String LOCAL_DB_PORT = "3306"; // Default MySQL port
    private static final String LOCAL_DB_NAME = "stud"; // Your local database name
    private static final String LOCAL_DB_USERNAME = "root"; // As provided by you
    private static final String LOCAL_DB_PASSWORD = "root"; // As provided by you


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
        boolean connectedToAiven = false;

        // --- Attempt to connect to Aiven MySQL first ---
        String aivenHost = System.getenv(AIVEN_HOST_ENV);
        String aivenPort = System.getenv(AIVEN_PORT_ENV);
        String aivenDatabase = System.getenv(AIVEN_DATABASE_ENV);
        String aivenUsername = System.getenv(AIVEN_USERNAME_ENV);
        String aivenPassword = System.getenv(AIVEN_PASSWORD_ENV);

        // Check if all Aiven environment variables are present and not empty
        boolean useAivenEnv = aivenHost != null && !aivenHost.isEmpty() &&
                              aivenPort != null && !aivenPort.isEmpty() &&
                              aivenDatabase != null && !aivenDatabase.isEmpty() &&
                              aivenUsername != null && !aivenUsername.isEmpty() &&
                              aivenPassword != null && !aivenPassword.isEmpty();

        if (useAivenEnv) {
            try {
                // Aiven requires SSL, so 'sslmode=require' is essential.
                // Keeping serverTimezone=UTC and allowPublicKeyRetrieval=true for consistency.
                finalJdbcUrl = String.format("jdbc:mysql://%s:%s/%s?sslmode=require&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                                              aivenHost, aivenPort, aivenDatabase);
                dbUser = aivenUsername;
                dbPassword = aivenPassword;

                System.out.println("✅ Found Aiven environment variables. Attempting Aiven DB connection.");
                System.out.println("    - Attempting to connect to: " + finalJdbcUrl);
                System.out.println("    - User: " + dbUser);

                conn = DriverManager.getConnection(finalJdbcUrl, dbUser, dbPassword);
                connectedToAiven = true;
                System.out.println("✅✅✅ Aiven Database connection SUCCESSFUL!");
                return conn; // Successfully connected to Aiven
            } catch (SQLException e) {
                System.err.println("⚠️ Failed to connect to Aiven MySQL database. Attempting local DB as fallback.");
                System.err.println("    - Aiven Connection Error: " + e.getMessage());
                // Do not re-throw, proceed to local connection attempt
            }
        } else {
            System.out.println("⚠️ Aiven MySQL environment variables not fully set. Attempting local database connection.");
        }

        // --- Fallback to Local MySQL connection if Aiven connection failed or not attempted ---
        if (!connectedToAiven) {
            try {
                // For local, useSSL=false is generally appropriate.
                finalJdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                                              LOCAL_DB_HOST, LOCAL_DB_PORT, LOCAL_DB_NAME);
                dbUser = LOCAL_DB_USERNAME;
                dbPassword = LOCAL_DB_PASSWORD;

                System.out.println("    - Attempting to connect to: " + finalJdbcUrl);
                System.out.println("    - User: " + dbUser);

                conn = DriverManager.getConnection(finalJdbcUrl, dbUser, dbPassword);
                System.out.println("✅✅✅ Local Database connection SUCCESSFUL!");
                return conn; // Successfully connected to Local DB
            } catch (SQLException e) {
                System.err.println("�🚫🚫 DATABASE CONNECTION FAILED for both Aiven and Local!");
                System.err.println("    - Final URL Attempted (Local): " + finalJdbcUrl);
                System.err.println("    - SQL State: " + e.getSQLState());
                System.err.println("    - Error Code: " + e.getErrorCode());
                System.err.println("    - Message: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Database connection failed for both Aiven and Local", e);
            }
        }
        // This part should ideally not be reached if either connection succeeds or throws RuntimeException
        return null;
    }

    /**
     * Closes a database connection.
     * @param conn The Connection object to close.
     */
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

    /**
     * Closes a PreparedStatement.
     * @param preparedStatement The PreparedStatement object to close.
     */
    public static void closePreparedStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                System.err.println("Error closing PreparedStatement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes a ResultSet.
     * @param resultSet The ResultSet object to close.
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
