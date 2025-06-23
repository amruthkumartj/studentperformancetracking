package com.portal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO {
	

	// Add this new method inside the UserDAO class
	public List<Student> getAllStudents() {
	    List<Student> students = new ArrayList<>();
	    String sql = "SELECT stud_id, name, course, sem, phone, email FROM students";

	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            Student student = new Student();
	            // Map database columns to our Student object fields
	            student.setStudentId(rs.getInt("stud_id"));
	            student.setFullName(rs.getString("name"));
	            student.setCourse(rs.getString("course"));
	            student.setSemester(rs.getInt("sem"));
	            student.setPhone(rs.getString("phone"));
	            student.setEmail(rs.getString("email"));
	            
	            students.add(student);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace(); // Log the error
	    }
	    return students;
	}
	public boolean deleteStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE stud_id = ?"; // Adjust table/column name
        try (Connection con = DBUtil.getConnection(); // Your method to get database connection
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Returns true if a row was deleted
        }
    }
	public boolean studentIdExists(int id) {
	    String sql = "SELECT 1 FROM students WHERE stud_id = ?";
	    try (Connection c = DBUtil.getConnection();
	         PreparedStatement ps = c.prepareStatement(sql)) {
	        ps.setInt(1, id);
	        return ps.executeQuery().next();
	    } catch (Exception ex) { ex.printStackTrace(); }
	    return false;
	}

	public boolean emailExists(String email) {
	    String sql = "SELECT 1 FROM students WHERE email = ?";
	    try (Connection c = DBUtil.getConnection();
	         PreparedStatement ps = c.prepareStatement(sql)) {
	        ps.setString(1, email);
	        return ps.executeQuery().next();
	    } catch (Exception ex) { ex.printStackTrace(); }
	    return false;
	}

	public boolean phoneExists(String phone) {
	    String sql = "SELECT 1 FROM students WHERE phone = ?";
	    try (Connection c = DBUtil.getConnection();
	         PreparedStatement ps = c.prepareStatement(sql)) {
	        ps.setString(1, phone);
	        return ps.executeQuery().next();
	    } catch (Exception ex) { ex.printStackTrace(); }
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
		        stmt.setString(5, phone);  // Changed to setString
		        stmt.setString(6, email);

		        int rowsInserted = stmt.executeUpdate();
		        return rowsInserted > 0;
		    }catch (SQLException e) {
		        e.printStackTrace();      // ← keep this
		        return false;
		    }
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
    
    public int register(String username, String password, String role) {
        int userId = -1;

        try (Connection conn = DBUtil.getConnection()) {
            // Check if user already exists
            String checkQuery = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("⚠️ Username already exists: " + username);
                    return -1; // Username already exists
                }
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (username, pwd_hash, role) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, role);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }
}