// src/main/java/com/portal/dao/FacultyDAO.java
package com.portal.datatransfer_access; // <--- Corrected package name as discussed

import com.portal.DBUtil; // Your database utility for connections
import com.portal.User; // Potentially used for User objects if any method returns one
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacultyDAO {

    /**
     * Adds a new faculty record to the 'faculty' table.
     * This should be called after a user account with role 'FACULTY' is approved.
     * @param userId The user_id from the 'users' table associated with this faculty.
     * @param fullName The full name of the faculty.
     * @param designation The designation of the faculty (e.g., "Professor", "Lecturer").
     * @return true if the faculty record was added successfully, false otherwise.
     */
    public boolean addFaculty(int userId, String fullName, String designation) {
        String sql = "INSERT INTO faculty (user_id, full_name, designation) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, designation);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // MySQL Duplicate entry error code
                System.err.println("⚠️ Duplicate entry: Faculty with user_id " + userId + " already exists.");
            } else {
                System.err.println("❌ SQL Error adding faculty: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }
    
    
    
 // In FacultyDAO.java
 // Make sure to import java.util.Map and java.util.HashMap

 // In FacultyDAO.java

 // In your FacultyDAO.java file, replace the entire method with this one.

    public Map<String, String> getFacultyProfileDetails(int userId) {
        Map<String, String> profileDetails = new HashMap<>();
        
        // === FINAL FIX: Removed "f.phone" from the SQL query as the column does not exist ===
        String sql = "SELECT u.username, u.email, f.faculty_id " +
                     "FROM users u JOIN faculty f ON u.user_id = f.user_id " +
                     "WHERE u.user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    profileDetails.put("username", rs.getString("username"));
                    profileDetails.put("email", rs.getString("email"));
                    profileDetails.put("facultyId", String.valueOf(rs.getInt("faculty_id")));
                    // We do not fetch a phone number here because it doesn't exist in the table.
                    // The JavaScript will handle displaying "Not Set".
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profileDetails;
    }
    /**
     * Counts the number of courses associated with programs assigned to a specific faculty member.
     * This assumes:
     * - Faculty are assigned to programs via the 'FacultyPrograms' table.
     * - Courses belong to programs via a 'program_id' column in the 'Courses' table.
     *
     * @param facultyId The faculty_id from the 'faculty' table.
     * @return The number of courses associated with the faculty's assigned programs.
     */
    public int getAssignedCoursesCount(int facultyId) { // Changed parameter to facultyId for consistency with 'assignProgramToFaculty'
        int count = 0;
        String sql = "SELECT COUNT(DISTINCT C.course_id) " +
                     "FROM courses C " +
                     "JOIN programs P ON C.program_id = P.program_id " +
                     "JOIN facultyprograms FP ON P.program_id = FP.program_id " +
                     "WHERE FP.faculty_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facultyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error getting assigned courses count for faculty (via programs): " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Retrieves the faculty_id from the 'faculty' table using the associated user_id.
     * This is useful after adding a faculty, to get their auto-incremented faculty_id.
     * @param userId The user_id from the 'users' table.
     * @return The faculty_id, or -1 if not found or an error occurs.
     */
    public int getFacultyIdByUserId(int userId) {
        String sql = "SELECT faculty_id FROM faculty WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("faculty_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error getting faculty ID by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Or throw an exception if not found
    }

    /**
     * Assigns a program to a faculty member by inserting a record into the FacultyPrograms linking table.
     * @param facultyId The ID of the faculty member (from the 'faculty' table).
     * @param programId The ID of the program to assign.
     * @return true if assignment was successful, false otherwise.
     */
    public boolean assignProgramToFaculty(int facultyId, int programId) {
        String sql = "INSERT INTO facultyprograms (faculty_id, program_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facultyId);
            pstmt.setInt(2, programId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // MySQL Duplicate entry error code
                System.err.println("⚠️ Duplicate entry: Faculty " + facultyId + " already assigned to program " + programId + ".");
            } else {
                System.err.println("❌ SQL Error assigning program to faculty: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the count of programs assigned to a specific faculty member.
     * This uses the FacultyPrograms linking table.
     * @param facultyId The faculty_id from the 'faculty' table.
     * @return The number of programs assigned to the faculty.
     */
    public int getAssignedProgramsCount(int facultyId) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM facultyprograms WHERE faculty_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facultyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error getting assigned programs count: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Retrieves a list of programs assigned to a specific faculty member.
     * This method is crucial for the faculty dashboard display.
     *
     * @param facultyId The ID of the faculty member.
     * @return A List of String arrays, where each array contains [program_id, program_name].
     * @throws SQLException If a database access error occurs.
     */
    public List<String[]> getAssignedPrograms(int facultyId) throws SQLException {
        List<String[]> assignedPrograms = new ArrayList<>();
        String sql = "SELECT p.program_id, p.program_name " +
                     "FROM programs p " +
                     "JOIN facultyprograms fp ON p.program_id = fp.program_id " +
                     "WHERE fp.faculty_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facultyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String programId = String.valueOf(rs.getInt("program_id"));
                    String programName = rs.getString("program_name");
                    assignedPrograms.add(new String[]{programId, programName});
                }
            }
        }
        return assignedPrograms;
    }
}