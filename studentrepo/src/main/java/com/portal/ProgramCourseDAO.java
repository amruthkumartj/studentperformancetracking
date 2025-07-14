package com.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// IMPORTS ADDED:
import com.portal.Course; // Assuming ProgramDAO will eventually contain program-specific methods

public class ProgramCourseDAO {

    // NOTE: It is recommended to create a separate ProgramDAO for program-specific
    // methods like addProgram, programExists, getAllPrograms (returning Program objects),
    // getProgramById, and getTotalPrograms.
    // However, for now, if you intend to keep them here, the methods are functional.

    /**
     * Adds a new academic program to the Programs table.
     * @param programName The name of the program (e.g., "BCA", "MCA").
     * @return true if the program was added successfully, false otherwise.
     */
    public boolean addProgram(String programName) {
        String sql = "INSERT INTO Programs (program_name) VALUES (?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, programName.trim());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Error adding program: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a program name already exists.
     * @param programName The name of the program to check.
     * @return true if the program exists, false otherwise.
     */
    public boolean programExists(String programName) {
        String sql = "SELECT 1 FROM Programs WHERE program_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, programName.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error checking program existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all academic programs from the Programs table.
     * This method is redundant if you're using `com.portal.dao.ProgramDAO.getAllPrograms()`
     * which returns `List<Program>`. It's recommended to migrate this to `ProgramDAO`
     * and use `List<Program>` for consistency.
     * @return A List of String arrays, where each array is {program_id, program_name}.
     */
    public List<String[]> getAllPrograms() { // Consider deprecating this in favor of ProgramDAO's List<Program>
        List<String[]> programs = new ArrayList<>();
        String sql = "SELECT program_id, program_name FROM Programs ORDER BY program_name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                programs.add(new String[]{
                    String.valueOf(rs.getInt("program_id")),
                    rs.getString("program_name")
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error fetching all programs (String[]): " + e.getMessage());
            e.printStackTrace();
        }
        return programs;
    }

    /**
     * Retrieves a Program object by its ID.
     * This method should ideally be in `com.portal.dao.ProgramDAO`.
     * @param programId The ID of the program.
     * @return A Program object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Program getProgramById(int programId) throws SQLException {
        // RECOMMENDED: Instead of duplicating logic, you can delegate to the dedicated ProgramDAO
        // ProgramDAO programDAO = new ProgramDAO();
        // return programDAO.getProgramById(programId);

        String sql = "SELECT program_id, program_name FROM Programs WHERE program_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, programId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Program program = new Program();
                    program.setProgramId(rs.getInt("program_id"));
                    program.setProgramName(rs.getString("program_name"));
                    return program;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the total count of academic programs from the Programs table.
     * This method should ideally be in `com.portal.dao.ProgramDAO`.
     * @return The total number of programs, or 0 if an error occurs.
     */
    public int getTotalPrograms() {
        // RECOMMENDED: Instead of duplicating logic, you can delegate to the dedicated ProgramDAO
        // ProgramDAO programDAO = new ProgramDAO();
        // return programDAO.getTotalPrograms();

        int totalPrograms = 0;
        String sql = "SELECT COUNT(*) FROM Programs";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                totalPrograms = rs.getInt(1); // Get the count from the first column
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error getting total programs: " + e.getMessage());
            e.printStackTrace();
        }
        return totalPrograms;
    }


    /**
     * Adds a new course to the Courses table.
     * @param programId The ID of the program this course belongs to.
     * @param semester The semester the course is offered.
     * @param courseId The unique code for the course (e.g., "CS101"). Mapped to 'course_id' in DB.
     * @param courseName The full name of the course (e.g., "Data Structures").
     * @return true if the course was added successfully, false otherwise.
     */
    public boolean addCourse(int programId, int semester, String courseId, String courseName) {
        String sql = "INSERT INTO Courses (program_id, semester, course_id, course_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            stmt.setInt(2, semester);
            stmt.setString(3, courseId.trim());
            stmt.setString(4, courseName.trim());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            // Check for duplicate entry error (Error Code 1062 for MySQL)
            if (e.getErrorCode() == 1062) {
                System.err.println("⚠️ Duplicate course entry: A course with ID '" + courseId +
                                   "' already exists for program ID " + programId + " in semester " + semester);
            } else {
                System.err.println("❌ SQL Error adding course: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a course with the given ID, program ID, and semester already exists.
     * @param programId The program ID.
     * @param semester The semester.
     * @param courseId The course ID to check.
     * @return true if the course exists, false otherwise.
     */
    public boolean courseExists(int programId, int semester, String courseId) {
        String sql = "SELECT 1 FROM Courses WHERE program_id = ? AND semester = ? AND course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            stmt.setInt(2, semester);
            stmt.setString(3, courseId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error checking course existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all courses, optionally filtered by program ID.
     * @param programId Optional program ID to filter courses. Pass 0 or negative to get all courses.
     * @return A List of Object arrays, where each array is {course_id (String), program_id (int), semester (int), course_name (String), program_name (String)}.
     */
    public List<Object[]> getAllCourses(int programId) {
        List<Object[]> courses = new ArrayList<>();
        String sql;
        if (programId > 0) {
            sql = "SELECT c.course_id, c.program_id, c.semester, c.course_name, p.program_name " +
                  "FROM Courses c JOIN Programs p ON c.program_id = p.program_id " +
                  "WHERE c.program_id = ? ORDER BY c.program_id, c.semester, c.course_name";
        } else {
            sql = "SELECT c.course_id, c.program_id, c.semester, c.course_name, p.program_name " +
                  "FROM Courses c JOIN Programs p ON c.program_id = p.program_id " +
                  "ORDER BY c.program_id, c.semester, c.course_name";
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (programId > 0) {
                stmt.setInt(1, programId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Object[]{
                        rs.getString("course_id"),
                        rs.getInt("program_id"),
                        rs.getInt("semester"),
                        rs.getString("course_name"),
                        rs.getString("program_name")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error fetching courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Retrieves the total number of students enrolled in a specific program.
     * @param programId The ID of the program.
     * @return The count of students in the program.
     * @throws SQLException If a database access error occurs.
     */
    public int getTotalStudentsInProgram(int programId) throws SQLException {
        int studentCount = 0;
        String sql = "SELECT COUNT(student_id) AS student_count FROM Students WHERE program_id = ?"; // Corrected table name to 'Students' (plural)

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, programId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    studentCount = rs.getInt("student_count");
                }
            }
        }
        return studentCount;
    }

    /**
     * Retrieves the total number of courses associated with a specific program.
     * @param programId The ID of the program.
     * @return The count of courses in the program.
     * @throws SQLException If a database access error occurs.
     */
    public int getTotalCoursesInProgram(int programId) throws SQLException {
        int courseCount = 0;
        String sql = "SELECT COUNT(course_id) AS course_count FROM Courses WHERE program_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, programId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    courseCount = rs.getInt("course_count");
                }
            }
        }
        return courseCount;
    }
}