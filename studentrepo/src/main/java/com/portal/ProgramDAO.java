package com.portal; // This should be the same package as your Program.java and Course.java

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
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Program program = new Program();
                program.setProgramId(rs.getInt("program_id"));
                program.setProgramName(rs.getString("program_name"));
                programs.add(program);
            }
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
}