// src/main/java/com/portal/ProgramDAO.java
package com.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProgramDAO {

    /**
     * Retrieves all academic programs from the Programs table.
     * @return A List of Program objects.
     * @throws SQLException If a database access error occurs.
     */
	   public List<Program> getAllPrograms() throws SQLException {
	        List<Program> programs = new ArrayList<>();
	        String sql = "SELECT program_id, program_name FROM programs ORDER BY program_name";
	        System.out.println("DEBUG ProgramDAO: Attempting to get connection for query: " + sql); // ADD THIS
	        try (Connection conn = DBUtil.getConnection();
	             PreparedStatement pstmt = conn.prepareStatement(sql);
	             ResultSet rs = pstmt.executeQuery()) {

	            System.out.println("DEBUG ProgramDAO: Connection and PreparedStatement successful. Executing query."); // ADD THIS

	            while (rs.next()) {
	                Program program = new Program();
	                program.setProgramId(rs.getInt("program_id"));
	                program.setProgramName(rs.getString("program_name"));
	                programs.add(program);
	                System.out.println("DEBUG ProgramDAO: Added program: " + program.getProgramName() + " (ID: " + program.getProgramId() + ")"); // ADD THIS
	            }
	            System.out.println("DEBUG ProgramDAO: Finished processing ResultSet. Total programs found: " + programs.size()); // ADD THIS
	        } catch (SQLException e) {
	            System.err.println("ERROR ProgramDAO: SQLException in getAllPrograms(): " + e.getMessage()); // ADD THIS
	            e.printStackTrace(); // Print full stack trace for detailed error
	            throw e; // Re-throw to ensure calling servlet handles it
	        }
	        return programs;
	    }


    /**
     * Retrieves a Program object by its ID.
     * @param programId The ID of the program.
     * @return A Program object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Program getProgramById(int programId) throws SQLException {
        String sql = "SELECT program_id, program_name FROM programs WHERE program_id = ?";
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
     * @return The total number of programs.
     * @throws SQLException If a database access error occurs.
     */
    public int getTotalPrograms() throws SQLException {
        int totalPrograms = 0;
        String sql = "SELECT COUNT(*) FROM programs";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                totalPrograms = rs.getInt(1);
            }
        }
        return totalPrograms;
    }

    /**
     * Adds a new academic program to the Programs table.
     * @param programName The name of the program (e.g., "BCA", "MCA").
     * @return true if the program was added successfully, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean addProgram(String programName) throws SQLException {
        String sql = "INSERT INTO programs (program_name) VALUES (?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, programName.trim());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Checks if a program name already exists.
     * @param programName The name of the program to check.
     * @return true if the program exists, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean programExists(String programName) throws SQLException {
        String sql = "SELECT 1 FROM programs WHERE program_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, programName.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Retrieves all unique semesters available for a given program.
     * This assumes semesters are stored in an Enrollments or Courses table.
     *
     * @param programId The ID of the program.
     * @return A list of unique semester numbers (Integers), sorted.
     * @throws SQLException if a database access error occurs.
     */
    public List<Integer> getSemestersByProgram(int programId) throws SQLException {
        List<Integer> semesters = new ArrayList<>();
        // Assuming semesters are linked to courses within a program
        String sql = "SELECT DISTINCT semester FROM courses WHERE program_id = ? ORDER BY semester ASC";

        System.out.println("DEBUG DAO: getSemestersByProgram - SQL: " + sql);
        System.out.println("DEBUG DAO: getSemestersByProgram - Params: programId=" + programId);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, programId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    semesters.add(rs.getInt("semester"));
                }
            }
        }
        System.out.println("DEBUG DAO: getSemestersByProgram - Found " + semesters.size() + " semesters for program " + programId + ".");
        return semesters;
    }
}
