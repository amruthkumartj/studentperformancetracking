// src/main/java/com/portal/MarksDAO.java
package com.portal.datatransfer_access;

import com.portal.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types; // Import for java.sql.Types
import java.util.ArrayList;
import java.util.List;

public class MarksDAO {

    public List<StudentMarkEntryDTO> getStudentsForMarksEntry(int programId, int semester, String courseId, String examType) throws SQLException {
        List<StudentMarkEntryDTO> students = new ArrayList<>();
        Connection conn = null; // Declare connection outside try-with-resources for finally block
        PreparedStatement pstmt = null; // Declare pstmt outside try-with-resources for finally block
        ResultSet rs = null; // Declare rs outside try-with-resources for finally block

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT s.student_id, s.student_name, sc.enrollment_id, m.marks_obtained, " +
                         "m.exam_type, m.course_id, m.faculty_id " + // Include existing mark details if present
                         "FROM students s " +
                         "JOIN student_courses sc ON s.student_id = sc.student_id " +
                         "AND sc.program_id = ? AND s.sem = ? AND sc.course_id = ? " + // Using s.sem and sc.program_id/course_id from your schema
                         "LEFT JOIN marks m ON sc.enrollment_id = m.enrollment_id " +
                         "AND m.exam_type = ? " + // Condition for marks table must be in ON clause for LEFT JOIN
                         "AND m.course_id = ?"; // Condition for marks table must be in ON clause for LEFT JOIN

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, programId); // Set program_id for student_courses join
            pstmt.setInt(2, semester);  // Set semester for students join (s.sem)
            pstmt.setString(3, courseId); // Set course_id for student_courses join
            pstmt.setString(4, examType); // Set exam_type for marks join
            pstmt.setString(5, courseId); // Set course_id for marks join (important!)

            rs = pstmt.executeQuery();

            while (rs.next()) {
                StudentMarkEntryDTO student = new StudentMarkEntryDTO();
                student.setStudentId(rs.getInt("student_id"));
                student.setFullName(rs.getString("student_name")); // Using student_name as per your schema
                student.setEnrollmentId(rs.getInt("enrollment_id"));
                
                // IMPORTANT: Check for NULL explicitly for marks_obtained
                Object marksObj = rs.getObject("marks_obtained");
                if (marksObj != null) {
                    student.setMarksObtained(rs.getDouble("marks_obtained"));
                    // Also populate existing examType, courseId, facultyId from marks table
                    student.setExamType(rs.getString("exam_type"));
                    student.setCourseId(rs.getString("course_id"));
                    student.setFacultyId(rs.getInt("faculty_id"));
                } else {
                    student.setMarksObtained(null); // Explicitly set to null if no mark found
                    // For new entries, pre-fill based on filter selections for consistency
                    student.setExamType(examType);  
                    student.setCourseId(courseId);
                    // Faculty ID will be set on frontend via window.currentFacultyId before submission
                }
                students.add(student);
            }
        } finally {
            // Ensure all resources are closed even if an exception occurs
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(pstmt);
            DBUtil.closeConnection(conn);
        }
        return students;
    }

    public void saveMarks(List<StudentMarkEntryDTO> marksEntries) throws SQLException {
        Connection conn = null;
        PreparedStatement upsertPstmt = null;
        PreparedStatement deletePstmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            String upsertSql = "INSERT INTO marks (enrollment_id, exam_type, marks_obtained, faculty_id, course_id, recorded_at) " +
                               "VALUES (?, ?, ?, ?, ?, NOW()) " +
                               "ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained), faculty_id = VALUES(faculty_id), recorded_at = VALUES(recorded_at)";
            upsertPstmt = conn.prepareStatement(upsertSql);

            String deleteSql = "DELETE FROM marks WHERE enrollment_id = ? AND exam_type = ? AND course_id = ?";
            deletePstmt = conn.prepareStatement(deleteSql);

            for (StudentMarkEntryDTO entry : marksEntries) {
                if (entry.getMarksObtained() == null) {
                    // Execute delete for cleared marks (marks set to null in DTO)
                    deletePstmt.setInt(1, entry.getEnrollmentId());
                    deletePstmt.setString(2, entry.getExamType());
                    deletePstmt.setString(3, entry.getCourseId());
                    deletePstmt.addBatch();
                } else {
                    // Add batch for UPSERT (new or updated marks)
                    upsertPstmt.setInt(1, entry.getEnrollmentId());
                    upsertPstmt.setString(2, entry.getExamType());
                    if (entry.getMarksObtained() != null) {
                        upsertPstmt.setDouble(3, entry.getMarksObtained());
                    } else {
                        upsertPstmt.setNull(3, Types.DECIMAL); // Should not happen here if getMarksObtained() != null check passes
                    }
                    upsertPstmt.setInt(4, entry.getFacultyId());
                    upsertPstmt.setString(5, entry.getCourseId());
                    upsertPstmt.addBatch();
                }
            }

            int[] deleteResults = deletePstmt.executeBatch();
            int[] upsertResults = upsertPstmt.executeBatch();
            
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e; // Re-throw the exception
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restore auto-commit
            }
            DBUtil.closePreparedStatement(upsertPstmt);
            DBUtil.closePreparedStatement(deletePstmt);
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * Retrieves marks for a specific student's enrollment in a given course and exam type.
     * @param enrollmentId The enrollment ID of the student for the course.
     * @param courseId The ID of the course.
     * @param examType The type of exam (e.g., "CIE", "SEE").
     * @return The marks obtained as a Double, or null if no marks are found.
     * @throws SQLException If a database access error occurs.
     */
    public Double getMarksForStudentCourse(int enrollmentId, String courseId, String examType) throws SQLException {
        String sql = "SELECT marks_obtained FROM marks WHERE enrollment_id = ? AND course_id = ? AND exam_type = ?";
        Double marks = null;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            pstmt.setString(2, courseId);
            pstmt.setString(3, examType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    marks = rs.getDouble("marks_obtained");
                    if (rs.wasNull()) { // Check if the value was actually NULL in the DB
                        marks = null;
                    }
                }
            }
        }
        return marks;
    }
    public Double getSpecificMarksForStudentCourse(int enrollmentId, String courseId, String examType) throws SQLException {
        Double marks = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT m.marks_obtained FROM marks m " +
                         "WHERE m.enrollment_id = ? AND m.course_id = ? AND m.exam_type = ?"; // Corrected column name

            ps = conn.prepareStatement(sql);
            ps.setInt(1, enrollmentId);
            ps.setString(2, courseId);
            ps.setString(3, examType); // Directly use the provided examType string

            System.out.println("DEBUG MarksDAO Query: " + ps.toString()); // For debugging

            rs = ps.executeQuery();

            if (rs.next()) {
                marks = rs.getDouble("marks_obtained");
                if (rs.wasNull()) { // If the database value for marks_obtained was NULL
                    marks = null;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error fetching specific marks (enrollmentId: " + enrollmentId + ", courseId: " + courseId + ", examType: " + examType + "): " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing ResultSet in MarksDAO: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing PreparedStatement in MarksDAO: " + e.getMessage()); }
            if (conn != null) { try { conn.close(); } catch (SQLException e) { System.err.println("Error closing connection in MarksDAO: " + e.getMessage()); } }
        }
        return marks;
    }
}
