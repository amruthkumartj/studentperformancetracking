package com.portal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {

    private static HikariDataSource dataSource;

    static {
        // --- Configuration for the connection pool ---
        HikariConfig config = new HikariConfig();

        // Check for Aiven (Render) environment variables first
        String aivenHost = System.getenv("AIVEN_MYSQL_HOST");
        if (aivenHost != null && !aivenHost.isEmpty()) {
            System.out.println("✅ Found Aiven environment variables. Configuring HikariCP for Aiven DB.");
            String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?sslmode=require",
                System.getenv("AIVEN_MYSQL_HOST"),
                System.getenv("AIVEN_MYSQL_PORT"),
                System.getenv("AIVEN_MYSQL_DATABASE"));
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(System.getenv("AIVEN_MYSQL_USERNAME"));
            config.setPassword(System.getenv("AIVEN_MYSQL_PASSWORD"));
        } else {
            // Fallback to local configuration
            System.out.println("⚠️ Aiven environment variables not set. Configuring HikariCP for local DB.");
            config.setJdbcUrl("jdbc:mysql://localhost:3306/stud?useSSL=false&serverTimezone=UTC");
            config.setUsername("root");
            config.setPassword("root");
        }

        // --- Pool Settings (Tune as needed) ---
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10); // Max 10 connections in the pool
        config.setMinimumIdle(5);      // Keep at least 5 idle connections ready
        config.setConnectionTimeout(30000); // 30 seconds to wait for a connection
        config.setIdleTimeout(600000);   // 10 minutes for an idle connection to be retired
        config.setMaxLifetime(1800000);  // 30 minutes max lifetime for a connection

        // Initialize the dataSource
        dataSource = new HikariDataSource(config);
    }

    /**
     * Gets a connection from the connection pool.
     * @return A database Connection object.
     * @throws SQLException if a connection cannot be obtained.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Your existing close methods are fine and do not need to be changed.
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // This returns the connection to the pool
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}