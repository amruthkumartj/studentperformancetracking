package com.portal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Registers a new user in the database.
     * Now includes a check to prevent null or empty usernames.
     * @param username The username for the new user.
     * @param password The password for the new user.
     * @param role The role of the new user ('STUDENT' or 'FACULTY').
     * @return The generated user_id if successful, or -1 on failure.
     */
    public int register(String username, String password, String role) {
        // --- CORE FIX 1: Add a guard clause to prevent invalid inserts ---
        // If the username is null or just empty spaces, fail immediately.
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Registration DAO Error: Attempted to register a null or empty username.");
            return -1;
        }

        if (isUsernameTaken(username)) {
            System.out.println("⚠️ Username already exists: " + username);
            return -1;
        }

        int userId = -1;
        String insertQuery = "INSERT INTO users (username, pwd_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username.trim()); // Trim the username before inserting
            stmt.setString(2, password);
            stmt.setString(3, role);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the full error to the server console
            return -1; // Ensure failure is returned on any exception
        }
        return userId;
    }


    public String getStudentNameById(int studId) {
        String sql = "SELECT name FROM students WHERE stud_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if not found
    }

    public boolean isUsernameTaken(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- All other methods in your DAO remain the same ---
    public boolean studentIdExists(int id) {
        String sql = "SELECT 1 FROM students WHERE stud_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeQuery().next();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    public User validate(String username, String password, String role) {
        String sql = "SELECT * FROM users WHERE username = ? AND pwd_hash = ? AND role = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT stud_id, name, course, sem, phone, email FROM students";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getInt("stud_id"));
                student.setFullName(rs.getString("name"));
                student.setCourse(rs.getString("course"));
                student.setSemester(rs.getInt("sem"));
                student.setPhone(rs.getString("phone"));
                student.setEmail(rs.getString("email"));
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public boolean deleteStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE stud_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM students WHERE email = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean phoneExists(String phone) {
        String sql = "SELECT 1 FROM students WHERE phone = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone);
            return ps.executeQuery().next();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean addStudent(int studId, String name, String course, int sem, String phone, String email)
            throws SQLException {
        String sql = "INSERT INTO students (stud_id, name, course, sem, phone, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studId);
            stmt.setString(2, name);
            stmt.setString(3, course);
            stmt.setInt(4, sem);
            stmt.setString(5, phone);
            stmt.setString(6, email);
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
