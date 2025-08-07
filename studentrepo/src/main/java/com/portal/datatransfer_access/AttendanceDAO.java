// src/main/java/com/portal/AttendanceDAO.java
package com.portal.datatransfer_access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.portal.AttendanceRecord;
import com.portal.AttendanceSession;
import com.portal.DBUtil;
import com.portal.Student;

public class AttendanceDAO {
    // Removed the Connection field and constructor, as DBUtil will provide connections dynamically.

    public int createAttendanceSession(AttendanceSession session) throws SQLException {
        String sql = "INSERT INTO attendancesessions (course_id, topic, faculty_id, session_start_time, session_expiry_time, status, location) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); // Get connection from pool
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, session.getCourseId());
            ps.setString(2, session.getTopic());
            ps.setInt(3, session.getFacultyId());

            if (session.getSessionStartTime() != null) {
                ps.setTimestamp(4, Timestamp.from(session.getSessionStartTime()));
            } else {
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }
            if (session.getSessionExpiryTime() != null) {
                ps.setTimestamp(5, Timestamp.from(session.getSessionExpiryTime()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }

            ps.setString(6, session.getStatus());
            ps.setString(7, session.getLocation());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        session.setSessionId(generatedKeys.getInt(1));
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        } // Connection, PreparedStatement are auto-closed by try-with-resources
    }

    public AttendanceSession getAttendanceSession(int sessionId) throws SQLException {
        String sql = "SELECT asess.session_id, asess.course_id, asess.topic, asess.faculty_id, " +
                     "asess.session_start_time, asess.session_expiry_time, asess.status, asess.location, " +
                     "c.program_id, c.course_name, c.semester, p.program_name " +
                     "FROM attendancesessions asess " +
                     "JOIN courses c ON asess.course_id = c.course_id " +
                     "JOIN programs p ON c.program_id = p.program_id " +
                     "WHERE asess.session_id = ?";

        try (Connection conn = DBUtil.getConnection(); // Get connection from pool
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AttendanceSession session = new AttendanceSession();
                    session.setSessionId(rs.getInt("session_id"));
                    session.setCourseId(rs.getString("course_id"));
                    session.setTopic(rs.getString("topic"));
                    session.setFacultyId(rs.getInt("faculty_id"));

                    Timestamp startTimeStamp = rs.getTimestamp("session_start_time");
                    if (startTimeStamp != null) {
                        session.setSessionStartTime(startTimeStamp.toInstant());
                    } else {
                        session.setSessionStartTime(null);
                    }

                    Timestamp expiryTimeStamp = rs.getTimestamp("session_expiry_time");
                    if (expiryTimeStamp != null) {
                        session.setSessionExpiryTime(expiryTimeStamp.toInstant());
                    } else {
                        session.setSessionExpiryTime(null);
                    }

                    session.setStatus(rs.getString("status"));
                    session.setLocation(rs.getString("location"));

                    session.setProgramId(rs.getInt("program_id"));
                    session.setSubjectName(rs.getString("course_name"));
                    session.setSemester(rs.getInt("semester"));
                    session.setProgramName(rs.getString("program_name"));

                    return session;
                }
            }
        }
        return null;
    }


    /**
     * Retrieves attendance records with optional filters.
     * @param programId Optional: Filter by program. Use -1 or 0 for no filter.
     * @param semester Optional: Filter by semester. Use -1 or 0 for no filter.
     * @param subjectId Optional: Filter by subject. Use null or empty string for no filter. (Changed to String)
     * @param date Optional: Filter by specific date. Use null for no date filter. (Changed to java.util.Date)
     * @param studentSearch Optional: Filter by student ID or name. Use null or empty string for no filter.
     * @return List of AttendanceRecord objects.
     * @throws SQLException If a database error occurs.
     */
    public List<AttendanceRecord> getAttendanceRecords(
            int programId, int semester, String subjectId, java.util.Date date, String studentSearch) throws SQLException {

        List<AttendanceRecord> records = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    ar.attendance_id, ");
        sql.append("    ar.session_id, ");
        sql.append("    e.student_id, ");
        sql.append("    ar.status, ");
        sql.append("    s.student_name, ");
        sql.append("    ar.attendance_date, ");
        sql.append("    c.course_name AS subject_name ");
        sql.append("FROM ");
        sql.append("    attendancerecords ar ");
        sql.append("JOIN ");
        sql.append("    enrollments e ON ar.enrollment_id = e.enrollment_id ");
        sql.append("JOIN ");
        sql.append("    students s ON e.student_id = s.student_id ");
        sql.append("JOIN ");
        sql.append("    attendancesessions asess ON ar.session_id = asess.session_id "); // Join AttendanceSessions
        sql.append("JOIN ");
        sql.append("    courses c ON asess.course_id = c.course_id "); // Join courses via AttendanceSessions.course_id

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (programId > 0) {
            conditions.add("e.program_id = ?"); // Assuming enrollments has program_id
            params.add(programId);
        }
        if (semester > 0) {
            conditions.add("c.semester = ?"); // Assuming 'semester' column is in the 'courses' table
            params.add(semester);
        }
        if (subjectId != null && !subjectId.trim().isEmpty()) {
            conditions.add("asess.course_id = ?"); // Filter by course_id from AttendanceSessions
            params.add(subjectId.trim());
        }
        if (date != null) {
            conditions.add("ar.attendance_date = ?");
            params.add(new java.sql.Date(date.getTime()));
        }
        if (studentSearch != null && !studentSearch.trim().isEmpty()) {
            try {
                int studentId = Integer.parseInt(studentSearch.trim());
                conditions.add("e.student_id = ?");
                params.add(studentId);
            } catch (NumberFormatException e) {
                conditions.add("s.student_name LIKE ?");
                params.add("%" + studentSearch.trim() + "%");
            }
        }


        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        sql.append(" ORDER BY ar.attendance_date DESC, s.student_name ASC");

        System.out.println("DEBUG: getAttendanceRecords SQL: " + sql.toString());
        System.out.println("DEBUG: getAttendanceRecords Params: " + params);

        try (Connection conn = DBUtil.getConnection(); // Get connection from pool
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setRecordId(rs.getInt("attendance_id"));
                    record.setSessionId(rs.getString("session_id")); // Assuming session_id is string in AttendanceRecord DTO
                    record.setStudentId(rs.getInt("student_id"));
                    record.setAttendanceStatus(rs.getString("status"));
                    record.setStudentName(rs.getString("student_name"));
                    record.setAttendanceDate(rs.getDate("attendance_date"));
                    record.setSubjectName(rs.getString("subject_name"));
                    records.add(record);
                }
            }
        }
        return records;
    }

    public boolean updateAttendanceSessionStatus(Connection conn, int sessionId, String status) throws SQLException { // Add conn parameter
        String sql = "UPDATE attendancesessions SET status = ? WHERE session_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) { // Use the passed conn
            ps.setString(1, status);
            ps.setInt(2, sessionId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public List<Student> getStudentsByProgramAndSemester(int programId, int semester) throws SQLException {
        List<Student> students = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT s.student_id, s.student_name, s.email, s.phone, s.sem, p.program_name, s.program_id ");
        sql.append("FROM students s JOIN programs p ON s.program_id = p.program_id ");
        sql.append("WHERE s.program_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(programId);

        if (semester > 0) {
            sql.append("AND s.sem = ?");
            params.add(semester);
        }

        sql.append(" ORDER BY s.student_name ASC");

        System.out.println("DEBUG: getStudentsByProgramAndSemester SQL: " + sql.toString());
        System.out.println("DEBUG: getStudentsByProgramAndSemester Params: " + params);

        try (Connection conn = DBUtil.getConnection(); // Get connection from pool
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("student_id"));
                    student.setFullName(rs.getString("student_name"));
                    student.setEmail(rs.getString("email"));
                    student.setPhone(rs.getString("phone"));
                    student.setSemester(rs.getInt("sem"));
                    student.setProgramName(rs.getString("program_name"));
                    student.setProgramId(rs.getInt("program_id"));
                    students.add(student);
                }
            }
        }
        return students;
    }

    public String getCourseIdBySubjectName(String subjectName, int programId, int semester) throws SQLException {
        String sql = "SELECT course_id FROM courses WHERE course_name = ? AND program_id = ? AND semester = ?";
        try (Connection conn = DBUtil.getConnection(); // Get connection from pool
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subjectName);
            ps.setInt(2, programId);
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("course_id");
                }
            }
        }
        return null;
    }

    public int getOrCreateEnrollment(Connection conn, int studentId, int academicYear, int programId) throws SQLException {
        // Step 1: First, try to SELECT the existing enrollment on the given connection.
        String selectSql = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND academic_year = ? AND program_id = ?";
        try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
            psSelect.setInt(1, studentId);
            psSelect.setInt(2, academicYear);
            psSelect.setInt(3, programId);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_id"); // Found it, return the existing ID.
                }
            }
        }

        // Step 2: If the SELECT returned nothing, then INSERT a new record on the same connection.
        String insertSql = "INSERT INTO enrollments (student_id, academic_year, program_id) VALUES (?, ?, ?)";
        try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            psInsert.setInt(1, studentId);
            psInsert.setInt(2, academicYear);
            psInsert.setInt(3, programId);
            int rowsAffected = psInsert.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Success! Return the newly generated ID.
                    }
                }
            }
        }
        
        // If we reach here, the insert failed for a reason other than the record existing.
        throw new SQLException("Failed to get or create enrollment for student " + studentId);
    }

    /**
     * ✅ CORRECTED: This method now accepts a Connection to participate in the servlet's transaction.
     */
    public boolean addMultipleAttendanceRecordsToAttendanceTable(
            Connection conn, // <-- Takes the connection as a parameter
            List<AttendanceRecord> attendanceRecords,
            String courseId,
            LocalDate attendanceDate,
            int academicYear,
            int programId,
            int sessionId
    ) throws SQLException {
        
        String sql = "INSERT INTO attendancerecords (enrollment_id, attendance_date, status, session_id) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = VALUES(status)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (AttendanceRecord record : attendanceRecords) {
                // Pass the connection to the helper method
                int enrollmentId = getOrCreateEnrollment(conn, record.getStudentId(), academicYear, programId);

                ps.setInt(1, enrollmentId);
                ps.setDate(2, java.sql.Date.valueOf(attendanceDate));
                
                String statusForDb;
                if ("P".equalsIgnoreCase(record.getAttendanceStatus())) {
                    statusForDb = "PRESENT";
                } else {
                    statusForDb = "ABSENT"; // Default to ABSENT
                }
                ps.setString(3, statusForDb);
                ps.setInt(4, sessionId);
                ps.addBatch();
            }
            
            ps.executeBatch();
            return true;

        } catch (SQLException e) {
            System.err.println("Error in addMultipleAttendanceRecordsToAttendanceTable: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Calculates the attendance percentage for a specific student in a given course, program, and semester.
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @param programId The ID of the program.
     * @param semester The semester.
     * @return The attendance percentage as a double (e.g., 85.5), or 0.0 if no sessions or records found.
     * @throws SQLException If a database access error occurs.
     */
    public double getAttendancePercentageForStudentCourse(int studentId, String courseId, int programId, int semester) throws SQLException {
        double attendancePercentage = 0.0;
        int totalSessions = 0;
        int presentSessions = 0;

        // Step 1: Get total sessions for the course, program, and semester
        String totalSessionsSql = "SELECT COUNT(DISTINCT asess.session_id) AS total_sessions " +
                                  "FROM attendancesessions asess " +
                                  "JOIN courses c ON asess.course_id = c.course_id " +
                                  "WHERE asess.course_id = ? AND c.program_id = ? AND c.semester = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement psTotal = conn.prepareStatement(totalSessionsSql)) {
            psTotal.setString(1, courseId);
            psTotal.setInt(2, programId);
            psTotal.setInt(3, semester);
            try (ResultSet rsTotal = psTotal.executeQuery()) {
                if (rsTotal.next()) {
                    totalSessions = rsTotal.getInt("total_sessions");
                }
            }
        }

        // Step 2: Get present sessions for the specific student in that course, program, and semester
        String presentSessionsSql = "SELECT COUNT(ar.attendance_id) AS present_sessions " +
                                    "FROM attendancerecords ar " +
                                    "JOIN attendancesessions asess ON ar.session_id = asess.session_id " +
                                    "JOIN student_courses sc ON ar.enrollment_id = sc.enrollment_id " +
                                    "WHERE sc.student_id = ? AND asess.course_id = ? AND ar.status = 'PRESENT' " +
                                    "AND sc.program_id = ? AND sc.semester = ?"; // Added program_id and semester filter for student_courses

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement psPresent = conn.prepareStatement(presentSessionsSql)) {
            psPresent.setInt(1, studentId);
            psPresent.setString(2, courseId);
            psPresent.setInt(3, programId); // Bind program_id
            psPresent.setInt(4, semester);   // Bind semester
            try (ResultSet rsPresent = psPresent.executeQuery()) {
                if (rsPresent.next()) {
                    presentSessions = rsPresent.getInt("present_sessions");
                }
            }
        }

        // Calculate percentage
        if (totalSessions > 0) {
            attendancePercentage = ((double) presentSessions / totalSessions) * 100.0;
        }

        return attendancePercentage;
    }
    public Map<String, Object> getAttendanceDetailsForStudentCourse(int studentId, String courseCode, int programId, int semester) throws SQLException {
        Map<String, Object> details = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement courseIdPs = null;
        ResultSet courseIdRs = null;

        int totalClassesHeld = 0;
        int classesAttended = 0;
        double percentage = 0.0;
        int numericCourseId = -1; // To store the looked-up numeric course ID

        try {
            conn = DBUtil.getConnection();

            // --- Step 1: Get the numeric course_id from the courseCode (e.g., "BCA101") ---
            String getCourseIdSql = "SELECT course_id FROM courses WHERE course_id = ?"; // Assuming course_id stores codes for lookup
            // OR if you added a course_code column:
            // String getCourseIdSql = "SELECT course_id FROM courses WHERE course_code = ?";

            // Given your previous 'c.course_id AS course_code' alias, it's very likely
            // that your `course_id` column in the `courses` table is actually storing the "codes"
            // (like 'BCA101') as VARCHAR, and you are using it as both ID and code.
            // Let's assume for a moment that `courses.course_id` is indeed VARCHAR and stores 'BCA101'
            // This is crucial. If `courses.course_id` is INT and `courses.course_code` is VARCHAR,
            // then change the WHERE clause here.

            // Re-confirming your 'courses' table schema is paramount here.
            // If 'courses.course_id' is an INT (e.g., 1, 2) and you have a 'courses.course_code' VARCHAR (e.g., 'BCA101')
            // Then this lookup would be:
            // String getCourseIdSql = "SELECT course_id FROM courses WHERE course_code = ?";
            // And the parameter below would be `courseCode`.

            // BUT, given `DEBUG AttendanceDAO Query: ... course_id = 'BCA101'`, it implies `course_id` is VARCHAR in `attendancesessions`
            // and you're directly comparing it. If `attendancesessions.course_id` is VARCHAR, then the join `ON ats.course_id = c.course_id`
            // implies that `c.course_id` must also be VARCHAR.

            // This is the most confusing part. Let's make an assumption to move forward,
            // and you should adjust if it's different.

            // ***ASSUMPTION: Your `courses.course_id` column is actually of type VARCHAR
            // and stores values like 'BCA101', acting as both the ID and the code.***
            // This makes `c.course_id = ?` directly valid for the `courseCode` string parameter.

            // If this assumption is correct, you do NOT need a lookup step.
            // The original logic with `c.course_id = ?` and `ps.setString(2, courseId)` would be correct.

            // --- Let's revert to the previous assumption where `courseId` (param) is string code,
            // --- `c.course_id` is string code, and `ats.course_id` is string code.
            // --- This aligns with the debug log: `course_id = 'BCA101'`

            // SQL Query for Attendance Summary
            String sql = "SELECT " +
                         "    SUM(CASE WHEN ar.status = 'Present' THEN 1 ELSE 0 END) AS classes_attended, " +
                         "    COUNT(DISTINCT ats.session_id) AS total_classes " +
                         "FROM " +
                         "    attendancerecords ar " +
                         "JOIN " +
                         "    attendancesessions ats ON ar.session_id = ats.session_id " +
                         "JOIN " +
                         "    enrollments e ON ar.enrollment_id = e.enrollment_id " +
                         "JOIN " +
                         "    courses c ON ats.course_id = c.course_id " + // This join implicitly assumes c.course_id and ats.course_id are compatible (e.g., both VARCHAR storing codes)
                         "WHERE " +
                         "    e.student_id = ? " +
                         "    AND c.course_id = ? " + // If c.course_id is VARCHAR and stores codes like 'BCA101'
                         "    AND c.program_id = ? " +
                         "    AND c.semester = ? " +
                         "    AND ats.status = 'Completed'";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);
            ps.setString(2, courseCode); // Use courseCode as it is, assuming c.course_id is VARCHAR and stores codes
            ps.setInt(3, programId);
            ps.setInt(4, semester);

            System.out.println("DEBUG AttendanceDAO Query: " + ps.toString()); // For debugging

            rs = ps.executeQuery();

            if (rs.next()) {
                totalClassesHeld = rs.getInt("total_classes");
                classesAttended = rs.getInt("classes_attended");
                if (totalClassesHeld > 0) {
                    percentage = (double) classesAttended / totalClassesHeld * 100.0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error fetching attendance details (studentId: " + studentId + ", courseId: " + courseCode + "): " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing ResultSet in AttendanceDAO: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing PreparedStatement in AttendanceDAO: " + e.getMessage()); }
            if (courseIdRs != null) try { courseIdRs.close(); } catch (SQLException e) { System.err.println("Error closing courseIdRs in AttendanceDAO: " + e.getMessage()); }
            if (courseIdPs != null) try { courseIdPs.close(); } catch (SQLException e) { System.err.println("Error closing courseIdPs in AttendanceDAO: " + e.getMessage()); }
            if (conn != null) { try { conn.close(); } catch (SQLException e) { System.err.println("Error closing connection in AttendanceDAO: " + e.getMessage()); } }
        }

        details.put("totalClasses", totalClassesHeld);
        details.put("classesAttended", classesAttended);
        details.put("percentage", percentage);
        return details;
    }
}
