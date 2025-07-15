package com.portal;

import java.time.LocalDate;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

import com.portal.Course;
import com.portal.Student;
import com.portal.User;

public class UserDAO {
/**update**/
    /**
     * Registers a new user in the database.
     * Passwords are now hashed using BCrypt.
     * Faculty users are registered with is_approved = FALSE by default.
     * @param username The username for the new user.
     * @param password The plain-text password for the new user.
     * @param role The role of the new user ('STUDENT' or 'FACULTY').
     * @return The generated user_id if successful, or -1 on failure.
     */
    public int register(String username, String password, String role) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Registration DAO Error: Attempted to register a null or empty username.");
            return -1;
        }

        if (isUsernameTaken(username)) {
            System.out.println("⚠️ Username already exists: " + username);
            return -1;
        }

        int userId = -1;
        // Hash the plain-text password using BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // For faculty, is_approved defaults to FALSE. For students, it can be TRUE immediately.
        // Assuming 'FACULTY' is the role that needs approval, 'STUDENT' is approved by default.
        boolean isApproved = role.equalsIgnoreCase("STUDENT") || role.equalsIgnoreCase("ADMIN"); // Admin should also be approved by default

        String insertQuery = "INSERT INTO users (username, pwd_hash, role, is_approved) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, hashedPassword); // Store the hashed password
            stmt.setString(3, role.toUpperCase());
            stmt.setBoolean(4, isApproved); // Set the approval status

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error during user registration: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return userId;
    }

    /**
     * Validates user credentials and approval status.
     * Now uses BCrypt to check passwords and includes is_approved status.
     * Special handling for 'admin' user: their actual role is checked, not the requestedRole from frontend.
     * @param username The username to validate.
     * @param password The plain-text password to validate.
     * @param requestedRole The role requested by the user from the frontend (e.g., 'STUDENT', 'FACULTY').
     * @return A User object if credentials are valid and user is approved (if faculty), otherwise null.
     */
    public User validate(String username, String password, String requestedRole) {
        String actualUsername = username.trim();
        String actualRequestedRole = requestedRole.trim().toUpperCase();

        System.out.println("DEBUG: UserDAO.validate called for username: '" + actualUsername + "', requestedRole (from frontend): '" + actualRequestedRole + "'");

        String sql = "SELECT user_id, username, role, pwd_hash, is_approved FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, actualUsername);
            System.out.println("DEBUG: Parameter 1 (username) set to: '" + actualUsername + "'");

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DEBUG: User found in database by username.");
                    String storedHashedPassword = rs.getString("pwd_hash");
                    String actualStoredRole = rs.getString("role");
                    boolean isApprovedFromDB = rs.getBoolean("is_approved"); // Correctly fetch is_approved

                    System.out.println("DEBUG: Stored Hash: " + storedHashedPassword);
                    System.out.println("DEBUG: Actual Stored Role (from DB): " + actualStoredRole);
                    System.out.println("DEBUG: Is Approved (from DB): " + isApprovedFromDB);

                    if (BCrypt.checkpw(password, storedHashedPassword)) {
                        System.out.println("DEBUG: Password matches (BCrypt check passed).");

                        if ("ADMIN".equalsIgnoreCase(actualStoredRole)) {
                            System.out.println("DEBUG: User is an ADMIN. Allowing login regardless of requested role.");
                            User user = new User();
                            user.setId(rs.getInt("user_id"));
                            user.setUsername(rs.getString("username"));
                            user.setRole(actualStoredRole);
                            user.setApproved(isApprovedFromDB); // Set the isApproved status
                            System.out.println("DEBUG: ADMIN user successfully validated and returned.");
                            return user;
                        }
                        else if (actualStoredRole.equalsIgnoreCase(actualRequestedRole)) {
                            System.out.println("DEBUG: Non-ADMIN user. Requested role matches stored role.");
                            // For FACULTY, check approval status
                            if ("FACULTY".equalsIgnoreCase(actualStoredRole) && !isApprovedFromDB) {
                                System.out.println("⚠️ Faculty user '" + actualUsername + "' is not yet approved. Returning null.");
                                return null; // Unapproved faculty
                            }

                            User user = new User();
                            user.setId(rs.getInt("user_id"));
                            user.setUsername(rs.getString("username"));
                            user.setRole(actualStoredRole);
                            user.setApproved(isApprovedFromDB); // Set the isApproved status
                            System.out.println("DEBUG: Student or approved Faculty user successfully validated and returned.");
                            return user;
                        } else {
                            System.out.println("❌ Role mismatch for non-ADMIN user: " + actualUsername + ". Stored role: " + actualStoredRole + ", Requested role: " + actualRequestedRole + ". Returning null.");
                            return null;
                        }
                    } else {
                        System.out.println("❌ Password mismatch for user: " + actualUsername + ". Returning null.");
                    }
                } else {
                    System.out.println("❌ User not found in database for username '" + actualUsername + "'. Returning null.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error during user validation: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ General Error during user validation: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("DEBUG: End of validate method, returning null (default path).");
        return null;
    }

    /**
     * Retrieves a User object by their user_id.
     * Assumes 'users' table has columns: user_id, username, role, is_approved.
     * @param userId The ID of the user to retrieve.
     * @return A User object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, role, is_approved FROM users WHERE user_id = ?";
        User user = null;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setApproved(rs.getBoolean("is_approved")); // Set the isApproved status
                }
            }
        }
        return user;
    }
    
    
    public int getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return -1; // User not found
    }

    /**
     * Retrieves a list of all faculty users who are currently not approved.
     * @return A List of User objects representing pending faculty.
     */
    public List<User> getPendingFaculty() throws SQLException { // MODIFIED: Added throws SQLException
        List<User> pendingFaculty = new ArrayList<>();
        String sql = "SELECT user_id, username, role, is_approved FROM users WHERE role = 'FACULTY' AND is_approved = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setApproved(rs.getBoolean("is_approved"));
                pendingFaculty.add(user);
            }
        }
        // REMOVED THE CATCH BLOCK FOR SQLException HERE. Let it propagate!
        return pendingFaculty;
    }

    /**
     * Approves a faculty user by setting their is_approved status to TRUE.
     * @param userId The user_id of the faculty member to approve.
     * @return true if the faculty was successfully approved, false otherwise.
     */
    public boolean approveFaculty(int userId) {
        String sql = "UPDATE users SET is_approved = TRUE WHERE user_id = ? AND role = 'FACULTY' AND is_approved = FALSE"; // Added AND is_approved = FALSE for safety
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Error approving faculty with ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Existing methods from your UserDAO.java, updated for new schema ---

    public String getStudentNameById(int studentId) {
        String sql = "SELECT student_name FROM students WHERE student_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("student_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUsernameTaken(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean studentIdExists(int id) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeQuery().next();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.student_name, s.program_id, p.program_name, s.sem, s.phone, s.email " +
                     "FROM students s JOIN Programs p ON s.program_id = p.program_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getInt("student_id"));
                student.setFullName(rs.getString("student_name"));
                student.setProgramId(rs.getInt("program_id"));
                student.setProgramName(rs.getString("program_name"));
                student.setSemester(rs.getInt("sem"));
                student.setPhone(rs.getString("phone"));
                student.setEmail(rs.getString("email"));
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error fetching all students: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }

    private List<Course> getCoursesByProgramAndSemester(int programId, int semester) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, course_name FROM courses WHERE program_id = ? AND semester = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, programId);
            pstmt.setInt(2, semester);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    courses.add(course);
                }
            }
        }
        return courses;
    }

    public boolean addStudent(int studentId, String name, int programId, int sem, String phone, String email)
            throws SQLException {
        Connection conn = null;
        PreparedStatement studentStmt = null;
        PreparedStatement studentCourseEnrollmentStmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String studentSql = "INSERT INTO students (student_id, student_name, program_id, sem, phone, email) VALUES (?, ?, ?, ?, ?, ?)";
            studentStmt = conn.prepareStatement(studentSql);
            studentStmt.setInt(1, studentId);
            studentStmt.setString(2, name);
            studentStmt.setInt(3, programId);
            studentStmt.setInt(4, sem);
            studentStmt.setString(5, phone);
            studentStmt.setString(6, email);

            int rowsInsertedStudents = studentStmt.executeUpdate();

            if (rowsInsertedStudents > 0) {
                System.out.println("DEBUG: Student record inserted successfully for studentId: " + studentId);

                List<Course> coursesToEnroll = getCoursesByProgramAndSemester(programId, sem);

                if (coursesToEnroll.isEmpty()) {
                    System.out.println("INFO: No courses found for programId " + programId + " and semester " + sem + ". Student " + studentId + " not enrolled in any courses.");
                    conn.commit();
                    success = true;
                } else {
                    String studentCourseSql = "INSERT INTO student_courses (student_id, course_id, program_id, semester, enrollment_date) VALUES (?, ?, ?, ?, CURDATE()) " +
                                              "ON DUPLICATE KEY UPDATE enrollment_date = VALUES(enrollment_date)";
                    studentCourseEnrollmentStmt = conn.prepareStatement(studentCourseSql);

                    for (Course course : coursesToEnroll) {
                        studentCourseEnrollmentStmt.setInt(1, studentId);
                        studentCourseEnrollmentStmt.setString(2, course.getCourseId());
                        studentCourseEnrollmentStmt.setInt(3, programId);
                        studentCourseEnrollmentStmt.setInt(4, sem);
                        studentCourseEnrollmentStmt.addBatch();
                    }

                    int[] rowsInsertedStudentCourses = studentCourseEnrollmentStmt.executeBatch();
                    boolean allCoursesEnrolled = true;
                    for (int result : rowsInsertedStudentCourses) {
                        if (result == Statement.EXECUTE_FAILED) {
                            allCoursesEnrolled = false;
                            break;
                        }
                    }

                    if (allCoursesEnrolled) {
                        conn.commit();
                        success = true;
                        System.out.println("DEBUG: Student " + studentId + " enrolled in " + coursesToEnroll.size() + " courses successfully.");
                    } else {
                        conn.rollback();
                        System.err.println("❌ Failed to enroll student " + studentId + " in all courses. Rolling back student insert.");
                    }
                }
            } else {
                conn.rollback();
                System.err.println("❌ Failed to insert student record for studentId: " + studentId);
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("❌ Transaction rolled back due to SQL error: " + e.getMessage());
                } catch (SQLException ex) {
                    System.err.println("❌ Error during rollback: " + ex.getMessage());
                }
            }
            System.err.println("❌ SQL Error adding student and/or enrollment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (studentCourseEnrollmentStmt != null) {
                try { studentCourseEnrollmentStmt.close(); } catch (SQLException e) { System.err.println("Error closing studentCourseEnrollmentStmt: " + e.getMessage()); }
            }
            if (studentStmt != null) {
                try { studentStmt.close(); } catch (SQLException e) { System.err.println("Error closing studentStmt: " + e.getMessage()); }
            }
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /* log if needed */ }
                try { conn.close(); } catch (SQLException e) { System.err.println("Error closing connection: " + e.getMessage()); }
            }
        }
        return success;
    }

    public boolean deleteStudent(String studentId) throws SQLException {
        Connection conn = null;
        PreparedStatement deleteAttendanceStmt = null;
        PreparedStatement deleteStudentCoursesStmt = null;
        PreparedStatement deleteStudentStmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int id = Integer.parseInt(studentId);

            String getEnrollmentIdsSql = "SELECT enrollment_id FROM student_courses WHERE student_id = ?";
            List<Integer> enrollmentIdsToDelete = new ArrayList<>();
            try(PreparedStatement ps = conn.prepareStatement(getEnrollmentIdsSql)) {
                ps.setInt(1, id);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        enrollmentIdsToDelete.add(rs.getInt("enrollment_id"));
                    }
                }
            }

            if (!enrollmentIdsToDelete.isEmpty()) {
                String placeholders = String.join(",", java.util.Collections.nCopies(enrollmentIdsToDelete.size(), "?"));
                String deleteAttendanceSql = "DELETE FROM attendancerecords WHERE enrollment_id IN (" + placeholders + ")";
                deleteAttendanceStmt = conn.prepareStatement(deleteAttendanceSql);
                for (int i = 0; i < enrollmentIdsToDelete.size(); i++) {
                    deleteAttendanceStmt.setInt(i + 1, enrollmentIdsToDelete.get(i));
                }
                int attendanceDeleted = deleteAttendanceStmt.executeUpdate();
                System.out.println("DEBUG: Deleted " + attendanceDeleted + " attendance records for student ID: " + id);
            }

            String deleteStudentCoursesSql = "DELETE FROM student_courses WHERE student_id = ?";
            deleteStudentCoursesStmt = conn.prepareStatement(deleteStudentCoursesSql);
            deleteStudentCoursesStmt.setInt(1, id);
            int studentCoursesDeleted = deleteStudentCoursesStmt.executeUpdate();
            System.out.println("DEBUG: Deleted " + studentCoursesDeleted + " student_courses records for student ID: " + id);

            String deleteStudentSql = "DELETE FROM students WHERE student_id = ?";
            deleteStudentStmt = conn.prepareStatement(deleteStudentSql);
            deleteStudentStmt.setInt(1, id);
            int studentsDeleted = deleteStudentStmt.executeUpdate();
            System.out.println("DEBUG: Deleted " + studentsDeleted + " student record for student ID: " + id);

            if (studentsDeleted > 0) {
                conn.commit();
                success = true;
                System.out.println("DEBUG: Student with ID " + id + " and all associated records successfully deleted.");
            } else {
                conn.rollback();
                System.err.println("DEBUG: Failed to delete student record for ID " + id + ". Rolling back transaction.");
            }

        } catch (NumberFormatException e) {
            System.err.println("❌ Error: Invalid studentId format for deletion: " + studentId);
            throw new SQLException("Invalid student ID format", e);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("❌ Transaction rolled back due to SQL error during student deletion: " + e.getMessage());
                } catch (SQLException ex) {
                    System.err.println("❌ Error during rollback: " + ex.getMessage());
                }
            }
            System.err.println("❌ SQL Error deleting student and/or related records: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (deleteAttendanceStmt != null) {
                try { deleteAttendanceStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteAttendanceStmt: " + e.getMessage()); }
            }
            if (deleteStudentCoursesStmt != null) {
                try { deleteStudentCoursesStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteStudentCoursesStmt: " + e.getMessage()); }
            }
            if (deleteStudentStmt != null) {
                try { deleteStudentStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteStudentStmt: " + e.getMessage()); }
            }
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /* log if needed */ }
                try { conn.close(); } catch (SQLException e) { System.err.println("Error closing connection: " + e.getMessage()); }
            }
        }
        return success;
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
}