package com.portal;

import java.sql.Connection;
// import java.sql.Date; // <--- REMOVE OR COMMENT OUT THIS LINE if it's there and causing conflict
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 // Make sure this is imported
import java.time.Instant; 
public class AttendanceDAO {
    private Connection connection;

    public AttendanceDAO(Connection connection) {
        this.connection = connection;
    }

    public int createAttendanceSession(AttendanceSession session) throws SQLException {
        String sql = "INSERT INTO attendancesessions (course_id, topic, faculty_id, session_start_time, session_expiry_time, status, location) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, session.getCourseId());
            ps.setString(2, session.getTopic());
            ps.setInt(3, session.getFacultyId());

            // --- CHANGE HERE for sessionStartTime ---
            if (session.getSessionStartTime() != null) {
                ps.setTimestamp(4, Timestamp.from(session.getSessionStartTime()));
            } else {
                ps.setNull(4, java.sql.Types.TIMESTAMP); // Handle null if possible
            }
            // --- CHANGE HERE for sessionExpiryTime ---
            if (session.getSessionExpiryTime() != null) {
                ps.setTimestamp(5, Timestamp.from(session.getSessionExpiryTime()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP); // Handle null if possible
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
        }
    }

    public AttendanceSession getAttendanceSession(int sessionId) throws SQLException {
        String sql = "SELECT asess.session_id, asess.course_id, asess.topic, asess.faculty_id, " +
                     "asess.session_start_time, asess.session_expiry_time, asess.status, asess.location, " +
                     "c.program_id, c.course_name, c.semester, p.program_name " +
                     "FROM attendancesessions asess " +
                     "JOIN courses c ON asess.course_id = c.course_id " +
                     "JOIN programs p ON c.program_id = p.program_id " +
                     "WHERE asess.session_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
                        // --- CHANGE HERE for sessionStartTime ---
                        session.setSessionStartTime(startTimeStamp.toInstant());
                    } else {
                        session.setSessionStartTime(null); // Or handle as appropriate if null is not allowed
                    }

                    Timestamp expiryTimeStamp = rs.getTimestamp("session_expiry_time");
                    if (expiryTimeStamp != null) {
                        // --- CHANGE HERE for sessionExpiryTime ---
                        session.setSessionExpiryTime(expiryTimeStamp.toInstant());
                    } else {
                        session.setSessionExpiryTime(null); // Or handle as appropriate if null is not allowed
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
            // *** CRUCIAL FIX: Changed from 'e.semester' to 'c.semester' ***
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
                // If it's a valid number, search ONLY by student ID
                conditions.add("e.student_id = ?");
                params.add(studentId);
            } catch (NumberFormatException e) {
                // If it's not a number (e.g., a name), search ONLY by student name
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

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setRecordId(rs.getInt("attendance_id"));
                    record.setSessionId(rs.getString("session_id"));
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
    public boolean updateAttendanceSessionStatus(int sessionId, String status) throws SQLException {
        String sql = "UPDATE attendancesessions SET status = ? WHERE session_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        if (semester > 0) { // Only add semester filter if a valid semester is provided
            sql.append("AND s.sem = ?");
            params.add(semester);
        }

        sql.append(" ORDER BY s.student_name ASC");

        System.out.println("DEBUG: getStudentsByProgramAndSemester SQL: " + sql.toString());
        System.out.println("DEBUG: getStudentsByProgramAndSemester Params: " + params);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    public int getOrCreateEnrollment(int studentId, int academicYear, int programId) throws SQLException {
        String selectSql = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND academic_year = ? AND program_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            ps.setInt(3, programId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_id");
                }
            }
        }

        String insertSql = "INSERT INTO enrollments (student_id, academic_year, program_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            ps.setInt(3, programId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to get or create enrollment for student " + studentId + " and program " + programId);
    }

    public boolean addMultipleAttendanceRecordsToAttendanceTable(
            List<AttendanceRecord> attendanceRecords,
            String courseId,
            LocalDate attendanceDate, // This is LocalDate
            int academicYear,
            int programId,
            int sessionId
    ) throws SQLException {
        String sql = "INSERT INTO attendancerecords (enrollment_id, attendance_date, status, session_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (AttendanceRecord record : attendanceRecords) {
                int enrollmentId = getOrCreateEnrollment(record.getStudentId(), academicYear, programId);

                ps.setInt(1, enrollmentId);
                // <<< FIX FOR Date.valueOf(attendanceDate) ERROR >>>
                ps.setDate(2, java.sql.Date.valueOf(attendanceDate)); // Correctly use java.sql.Date.valueOf(LocalDate)
                
                String statusForDb;
                if ("P".equalsIgnoreCase(record.getAttendanceStatus())) {
                    statusForDb = "PRESENT";
                } else if ("A".equalsIgnoreCase(record.getAttendanceStatus())) {
                    statusForDb = "ABSENT";
                } else {
                    statusForDb = "ABSENT"; // Default to ABSENT
                }
                ps.setString(3, statusForDb);
                ps.setInt(4, sessionId);
                ps.addBatch();
            }
            int[] rowsAffected = ps.executeBatch();
            for (int count : rowsAffected) {
                if (count == 0) return false;
            }
            return true;
        }
    }
}