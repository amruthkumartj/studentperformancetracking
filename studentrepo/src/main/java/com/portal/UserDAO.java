package com.portal;

import java.time.LocalDate;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

// Ensure these imports are correct based on your project structure
// For example, if your DTOs are in 'com.portal.model', adjust accordingly.
import com.portal.Course;
import com.portal.Student;
import com.portal.User;
import com.portal.StudentListItem;
import com.portal.StudentPerformance;
import com.portal.CoursePerformance;

// Assuming DBUtil and PasswordHasher are in 'com.portal.util' or similar
import com.portal.DBUtil;
// Removed import for PasswordHasher as it's not needed for checking, only for hashing during registration.


public class UserDAO {
    // Instantiate DAOs for marks and attendance to be used within this DAO
    // This is a common pattern for DAOs to collaborate.
    private MarksDAO marksDAO = new MarksDAO(); // Ensure MarksDAO is accessible
    private AttendanceDAO attendanceDAO = new AttendanceDAO(); // Ensure AttendanceDAO is accessible
    private ProgramCourseDAO programCourseDAO = new ProgramCourseDAO(); // Ensure ProgramCourseDAO is accessible

    // Helper method to check if a string is a valid email format
    private boolean isValidEmail(String email) {
        // Simple regex for email validation (can be more robust for production)
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|in|edu|org|net)$");
    }

    // Helper method to check if a string is purely numeric
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str); // Use Long to handle larger IDs if needed
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Retrieves a User object by their email address.
     * @param email The email address to search for.
     * @return A User object if found, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("pwd_hash"));
                    user.setRole(rs.getString("role"));
                    user.setEmail(rs.getString("email"));
                    user.setApproved(rs.getBoolean("is_approved"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a User object by their username.
     * @param username The username to search for.
     * @return A User object if found, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, email, pwd_hash, role, is_approved FROM users WHERE username = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("pwd_hash"), // Correct: passwordHash is 3rd param in User constructor
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getBoolean("is_approved")
                    );
                }
            }
        }
        return null;
    }
    
    
    
    
 // Add this method to find a user by their email
  

    // Add this method to update a user's password
    public boolean updatePassword(String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET pwd_hash = ? WHERE email = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a User object linked to a specific faculty ID.
     * This assumes the 'faculty' table has a 'user_id' column linking to the 'users' table.
     * @param facultyId The faculty ID to search for.
     * @return A User object if found, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserByFacultyId(int facultyId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.email, u.pwd_hash, u.role, u.is_approved " +
                     "FROM users u JOIN faculty f ON u.user_id = f.user_id WHERE f.faculty_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("pwd_hash"), // Correct: passwordHash is 3rd param in User constructor
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getBoolean("is_approved")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Registers a new user in the database.
     * Passwords are now hashed using BCrypt.
     * Faculty users are registered with is_approved = FALSE by default.
     * @param username The username for the new user.
     * @param password The plain-text password for the new user.
     * @param role The role of the new user ('STUDENT', 'FACULTY', 'ADMIN').
     * @param email The email address for the new user.
     * @return The generated user_id if successful, or -1 on failure.
     * @throws IllegalArgumentException if email domain is invalid.
     */
    public int register(String username, String password, String role, String email) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Registration DAO Error: Attempted to register a null or empty username.");
            return -1;
        }
        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ Registration DAO Error: Email cannot be null or empty.");
            return -1;
        }

        // --- NEW: Email Suffix Validation for all roles ---
        String lowerCaseEmail = email.trim().toLowerCase();
        if (!(lowerCaseEmail.endsWith(".com") || lowerCaseEmail.endsWith(".in") || lowerCaseEmail.endsWith(".edu"))) {
            System.err.println("❌ Email validation failed: Email must end with .com, .in, or .edu for " + username);
            throw new IllegalArgumentException("Invalid email domain. Email must end with .com, .in, or .edu.");
        }
        // --- END NEW ---

        // Check if username is already taken (pre-check, actual unique constraint is in DB)
       
        // Check if email is already taken in the users table
        if (emailExistsInUsersTable(email)) {
            System.out.println("⚠️ Email already exists for another user: " + email);
            return -2; // Return -2 for email taken
        }

        int userId = -1;
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // Correct for hashing during registration

        // For faculty, is_approved defaults to FALSE. For students/admins, it's TRUE immediately.
        boolean isApproved = role.equalsIgnoreCase("STUDENT") || role.equalsIgnoreCase("ADMIN");

        // --- MODIFIED INSERT QUERY: Added 'email' column ---
        String insertQuery = "INSERT INTO users (username, pwd_hash, role, email, is_approved) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role.toUpperCase());
            stmt.setString(4, lowerCaseEmail); // Store the validated email
            stmt.setBoolean(5, isApproved); // Set the approval status

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Catch specific exceptions for unique constraints
            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("users.username")) {
                System.err.println("❌ Registration DAO Error: Username already exists (SQL constraint): " + username);
                return -3; // Username already exists
            } else if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("users.email")) {
                System.err.println("❌ Registration DAO Error: Email already exists (SQL constraint): " + email);
                return -2; // Email already exists
            } else {
                System.err.println("❌ Error during user registration (SQLIntegrityConstraintViolation): " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error during user registration: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            System.err.println("❌ General Error during user registration: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return userId;
    }

    /**
     * Checks if an email already exists in the `users` table.
     * @param email The email to check.
     * @return true if the email exists, false otherwise.
     */
    public boolean emailExistsInUsersTable(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // True if a record is found
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking if email exists in users table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates user login credentials. This method is designed to be flexible and attempts to
     * identify the user's role and lookup method based on the identifier format,
     * as the frontend 'role' parameter might be fixed (e.g., always "faculty").
     *
     * @param identifier The username, email, or faculty ID provided by the user.
     * @param password The plain-text password.
     * @param requestedRole The role requested from the frontend (e.g., "faculty"). This parameter
     * is kept for signature compatibility but is largely ignored for the initial lookup.
     * @return A User object if credentials are valid and user is approved (if faculty), null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public User validate(String identifier, String password, String requestedRole) throws SQLException {
        User user = null;

        // 1. Try to validate as ADMIN (by username)
        user = getUserByUsername(identifier);
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            if (BCrypt.checkpw(password, user.getPasswordHash())) { // CORRECT BCrypt check
                System.out.println("Login attempt: Identified as ADMIN via username. Success.");
                return user; // Valid admin login
            } else {
                System.out.println("Login attempt: ADMIN password mismatch.");
                return null; // Password mismatch for admin
            }
        }

        // 2. If not Admin, try to validate as STUDENT or FACULTY by Email
        if (isValidEmail(identifier)) {
            user = getUserByEmail(identifier);
            if (user != null) {
                if (BCrypt.checkpw(password, user.getPasswordHash())) { // CORRECT BCrypt check
                    // Check approval for FACULTY
                    if ("FACULTY".equalsIgnoreCase(user.getRole()) && !user.isApproved()) {
                        System.out.println("Login attempt: Faculty account pending approval for email " + identifier);
                        return null; // Valid credentials, but not approved
                    }
                    System.out.println("Login attempt: Identified as " + user.getRole() + " via email. Success.");
                    return user; // Valid student or approved faculty login via email
                } else {
                    System.out.println("Login attempt: Email password mismatch for " + identifier);
                    return null; // Password mismatch
                }
            }
        }

        // 3. If not found by email or not an email, try to validate as FACULTY by Faculty ID (if numeric)
        if (isNumeric(identifier)) {
            try {
                int facultyId = Integer.parseInt(identifier);
                user = getUserByFacultyId(facultyId);
                if (user != null && "FACULTY".equalsIgnoreCase(user.getRole())) {
                    if (BCrypt.checkpw(password, user.getPasswordHash())) { // CORRECT BCrypt check
                        if (!user.isApproved()) {
                            System.out.println("Login attempt: Faculty account pending approval for ID " + identifier);
                            return null; // Valid credentials, but not approved
                        }
                        System.out.println("Login attempt: Identified as FACULTY via ID. Success.");
                        return user; // Valid approved faculty login via ID
                    } else {
                        System.out.println("Login attempt: Faculty ID password mismatch for " + identifier);
                        return null; // Password mismatch
                    }
                }
            } catch (NumberFormatException e) {
                // Should not happen due to isNumeric check, but good practice
                System.err.println("Error parsing numeric identifier in UserDAO.validate: " + identifier);
            }
        }

        System.out.println("Login attempt: No user found or invalid credentials for identifier: " + identifier);
        return null; // No matching user found for any role with given identifier and password
    }

    /**
     * Retrieves a User object by their user_id.
     * Assumes 'users' table has columns: user_id, username, role, is_approved, email, pwd_hash.
     * @param userId The ID of the user to retrieve.
     * @return A User object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, role, is_approved, email, pwd_hash FROM users WHERE user_id = ?";
        User user = null;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("pwd_hash"), // Populate passwordHash
                        rs.getString("role"),
                        rs.getString("email"),     // Populate email
                        rs.getBoolean("is_approved")
                    );
                }
            }
        }
        return user;
    }

    /**
     * Helper method to get user's email from the 'users' table using a faculty_id.
     * This implies a relationship where faculty_id (from 'faculty' table) links to 'users.user_id'.
     *
     * @param facultyId The numeric ID provided by the faculty for login.
     * @return The email associated with that faculty_id in the users table, or null if not found.
     */
    private String getEmailByFacultyId(int facultyId) {
        // This query assumes 'faculty.user_id' links to 'users.user_id'
        // and that 'users.email' is the definitive email for login.
        String sql = "SELECT u.email FROM users u " +
                     "JOIN faculty f ON u.user_id = f.user_id " +
                     "WHERE f.faculty_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facultyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error getting faculty email by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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
     * @throws SQLException If a database access error occurs.
     */
    public List<User> getPendingFaculty() throws SQLException {
        List<User> pendingFaculty = new ArrayList<>();
        String sql = "SELECT user_id, username, role, is_approved, email, pwd_hash FROM users WHERE role = 'FACULTY' AND is_approved = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("pwd_hash"), // Populate passwordHash
                    rs.getString("role"),
                    rs.getString("email"),     // Populate email
                    rs.getBoolean("is_approved")
                );
                pendingFaculty.add(user);
            }
        }
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

    /**
     * Retrieves a list of students for a given program and semester.
     * This method is now more specific to the student list display.
     * @param programId The ID of the program.
     * @param semester The semester.
     * @return A List of StudentListItem objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<StudentListItem> getStudentsByProgramAndSemesterForList(int programId, int semester) throws SQLException {
        List<StudentListItem> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.student_name, p.program_name, s.sem " +
                     "FROM students s " +
                     "JOIN programs p ON s.program_id = p.program_id " +
                     "WHERE s.program_id = ? AND s.sem = ? " +
                     "ORDER BY s.student_name ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, programId);
            pstmt.setInt(2, semester);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StudentListItem student = new StudentListItem();
                    student.setStudentId(String.valueOf(rs.getInt("student_id"))); // Convert int to String
                    student.setStudentName(rs.getString("student_name"));
                    student.setProgramName(rs.getString("program_name"));
                    student.setSemester(rs.getInt("sem"));
                    students.add(student);
                }
            }
        }
        return students;
    }

    /**
     * Retrieves all student records from the database.
     * This method is used when no specific program or semester filter is applied.
     * @return A List of Student objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.student_name, s.program_id, p.program_name, s.sem, s.phone, s.email " +
                     "FROM students s JOIN programs p ON s.program_id = p.program_id " +
                     "ORDER BY s.student_name ASC"; // Order by name for consistency

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
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
        }
        return students;
    }

    /**
     * Retrieves detailed performance for a single student.
     * This method aggregates data from students, courses, marks, and attendance.
     * @param studentId The ID of the student to retrieve performance for.
     * @param programId The program ID (for filtering relevant courses/enrollments).
     * @param semester The semester (for filtering relevant courses/enrollments).
     * @param examType This parameter is currently not used for filtering, but can be added if needed.
     * @return A StudentPerformance object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public StudentPerformance getStudentPerformance(int studentId, int programId, int semester, String examType) throws SQLException {
        StudentPerformance studentPerformance = null;
        Connection conn = null;

        // Define constants for max marks (ADJUST THESE BASED ON YOUR COLLEGE'S RULES)
        final double MAX_IA_MARKS = 50.0; // Max marks for Internal Assessment 1 and 2
        final double MAX_SEE_MARKS = 100.0; // Max marks for SEE

        try {
            conn = DBUtil.getConnection();

            // 1. Get basic student info, program name from enrollment, and semester
            String studentInfoSql = "SELECT s.student_id, s.student_name, p.program_name, s.sem " + // Added s.sem
                                    "FROM students s " +
                                    "JOIN enrollments e ON s.student_id = e.student_id " +
                                    "JOIN programs p ON e.program_id = p.program_id " +
                                    "WHERE s.student_id = ? AND e.program_id = ? AND s.sem = ?"; // Use s.sem for student's current semester

            System.out.println("DEBUG UserDAO: getStudentPerformance - studentInfoSql: " + studentInfoSql);
            System.out.println("DEBUG UserDAO: getStudentPerformance - studentInfo Params: studentId=" + studentId + ", programId=" + programId + ", semester=" + semester);


            try (PreparedStatement ps = conn.prepareStatement(studentInfoSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, programId);
                ps.setInt(3, semester);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentPerformance = new StudentPerformance();
                        studentPerformance.setStudentId(rs.getInt("student_id"));
                        studentPerformance.setStudentName(rs.getString("student_name"));
                        studentPerformance.setProgramName(rs.getString("program_name")); // Set program name
                        studentPerformance.setSemester(rs.getInt("sem")); // Set semester from student record
                        studentPerformance.setCoursePerformances(new ArrayList<>());
                    } else {
                        System.out.println("DEBUG UserDAO: Student not found for ID " + studentId + " in program " + programId + " semester " + semester);
                        return null; // Student not found
                    }
                }
            }

            if (studentPerformance == null) {
                return null;
            }

            // 2. Get courses for this student in the specified program and semester
            String coursesSql = "SELECT c.course_id, c.course_name, c.course_id AS course_code, sc.enrollment_id " +
                                "FROM courses c " +
                                "JOIN student_courses sc ON c.course_id = sc.course_id " +
                                "WHERE sc.student_id = ? AND c.program_id = ? AND c.semester = ?";

            List<CoursePerformance> coursePerformances = new ArrayList<>();
            System.out.println("DEBUG UserDAO: getStudentPerformance - coursesSql: " + coursesSql);
            System.out.println("DEBUG UserDAO: getStudentPerformance - course Params: studentId=" + studentId + ", programId=" + programId + ", semester=" + semester);

            try (PreparedStatement ps = conn.prepareStatement(coursesSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, programId);
                ps.setInt(3, semester);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String courseId = rs.getString("course_id"); // Use course_id as primary key for marks/attendance
                        String courseCode = rs.getString("course_code"); // For display
                        String subjectName = rs.getString("course_name"); // For display
                        int enrollmentId = rs.getInt("enrollment_id"); // Needed for Marks table

                        // 3. Fetch individual exam marks for this course using MarksDAO
                        Double ia1Marks = marksDAO.getSpecificMarksForStudentCourse(enrollmentId, courseId, "Internal Assessment 1");
                        Double ia2Marks = marksDAO.getSpecificMarksForStudentCourse(enrollmentId, courseId, "Internal Assessment 2");
                        Double seeMarks = marksDAO.getSpecificMarksForStudentCourse(enrollmentId, courseId, "SEE (Semester End Examination)");

                        // Calculate Combined CIE (Average of IA1 and IA2)
                        Double combinedCieMarks = null;
                        if (ia1Marks != null && ia2Marks != null) {
                            combinedCieMarks = (ia1Marks + ia2Marks) / 2.0;
                        } else if (ia1Marks != null) {
                            combinedCieMarks = ia1Marks; // If only IA1 exists, use it as combined CIE
                        } else if (ia2Marks != null) {
                            combinedCieMarks = ia2Marks; // If only IA2 exists, use it as combined CIE
                        }

                        // Calculate Overall Total Marks (Combined CIE Avg + SEE)
                        // Overall Max Marks for CIE (if using average) is MAX_IA_MARKS (e.g., 50)
                        Double overallTotalMarks = null;
                        Double overallMaxMarks = null;
                        Double overallPercentage = null;

                        if (combinedCieMarks != null && seeMarks != null) {
                            overallTotalMarks = combinedCieMarks + seeMarks;
                            overallMaxMarks = MAX_IA_MARKS + MAX_SEE_MARKS; // e.g., 50 (CIE avg) + 100 (SEE) = 150
                            if (overallMaxMarks > 0) {
                                overallPercentage = (overallTotalMarks / overallMaxMarks) * 100.0;
                            }
                        } else if (combinedCieMarks != null) { // Only CIE is available
                            overallTotalMarks = combinedCieMarks;
                            overallMaxMarks = MAX_IA_MARKS;
                            if (overallMaxMarks > 0) {
                                overallPercentage = (overallTotalMarks / overallMaxMarks) * 100.0;
                            }
                        } else if (seeMarks != null) { // Only SEE is available
                            overallTotalMarks = seeMarks;
                            overallMaxMarks = MAX_SEE_MARKS;
                            if (overallMaxMarks > 0) {
                                overallPercentage = (overallTotalMarks / overallMaxMarks) * 100.0;
                            }
                        }


                        // 4. Fetch attendance details for this course
                        Map<String, Object> attendanceDetails = attendanceDAO.getAttendanceDetailsForStudentCourse(
                            studentId, courseId, programId, semester);
                        double attendancePercentage = (double) attendanceDetails.getOrDefault("percentage", 0.0);
                        int totalClassesHeld = (int) attendanceDetails.getOrDefault("totalClasses", 0);
                        int classesAttended = (int) attendanceDetails.getOrDefault("classesAttended", 0);

                        System.out.println("DEBUG UserDAO: Course: " + subjectName + " (" + courseCode + ")");
                        System.out.println("DEBUG UserDAO:   IA1: " + ia1Marks + ", IA2: " + ia2Marks + ", SEE: " + seeMarks + ", Combined CIE: " + combinedCieMarks + ", Overall Total: " + overallTotalMarks);
                        System.out.println("DEBUG UserDAO:   Attendance: " + attendancePercentage + "% (" + classesAttended + "/" + totalClassesHeld + ")");


                        // Create CoursePerformance object with all details
                        CoursePerformance cp = new CoursePerformance(
                            courseCode, subjectName,
                            ia1Marks, ia2Marks, seeMarks,
                            combinedCieMarks,
                            overallTotalMarks, overallMaxMarks, overallPercentage,
                            attendancePercentage, totalClassesHeld, classesAttended
                        );
                        coursePerformances.add(cp);
                    }
                }
            }
            studentPerformance.setCoursePerformances(coursePerformances);

            // 5. Calculate overall analysis (based on OVERALL_TOTAL_MARKS from all courses)
            double totalOverallScoreForAllCourses = 0;
            double totalPossibleOverallScoreForAllCourses = 0;
            int analyzedCourseCount = 0;

            for (CoursePerformance cp : coursePerformances) {
                if (cp.getOverallTotalMarks() != null && cp.getOverallMaxMarks() != null && cp.getOverallMaxMarks() > 0) {
                    totalOverallScoreForAllCourses += cp.getOverallTotalMarks();
                    totalPossibleOverallScoreForAllCourses += cp.getOverallMaxMarks();
                    analyzedCourseCount++;
                }
            }

            String overallAnalysis = "N/A";
            if (analyzedCourseCount > 0 && totalPossibleOverallScoreForAllCourses > 0) {
                double aggregatePercentage = (totalOverallScoreForAllCourses / totalPossibleOverallScoreForAllCourses) * 100;

                if (aggregatePercentage >= 90) {
                    overallAnalysis = "Excellent";
                } else if (aggregatePercentage >= 80) {
                    overallAnalysis = "Very Good";
                } else if (aggregatePercentage >= 60) {
                    overallAnalysis = "Good";
                } else {
                    overallAnalysis = "Needs Improvement";
                }
            }
            studentPerformance.setOverallAnalysis(overallAnalysis);

            // Removed `setOverallCieResult` and `setOverallSeeResult` calls as per our previous fix
            // These individual overall results are now superseded by course-level breakdown
            // and the aggregate 'overallAnalysis' string.

        } catch (SQLException e) {
            System.err.println("❌ SQL Error fetching student performance in UserDAO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection in UserDAO: " + e.getMessage());
                }
            }
        }
        System.out.println("DEBUG UserDAO: getStudentPerformance - Returning performance for student: " + studentId);
        return studentPerformance;
    }

 // In com/portal/UserDAO.java

    public List<Student> searchStudentsByName(String searchTerm) throws SQLException {
        List<Student> students = new ArrayList<>();
        // Use LIKE for partial name matching
        String sql = "SELECT s.student_id, s.student_name, s.program_id, p.program_name, s.sem, s.phone, s.email " +
                     "FROM students s JOIN programs p ON s.program_id = p.program_id " +
                     "WHERE s.student_name LIKE ? ORDER BY s.student_name ASC LIMIT 10"; // Limit to 10 results

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm + "%"); // Wildcards for partial search
            
            try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        }
        return students;
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

                // Use ProgramCourseDAO to get courses
                List<Course> coursesToEnroll = programCourseDAO.getCoursesByProgramAndSemester(programId, sem); // Using programCourseDAO instance

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
        PreparedStatement deleteMarksStmt = null;
        PreparedStatement deleteAttendanceStmt = null;
        PreparedStatement deleteEnrollmentsStmt = null;
        PreparedStatement deleteStudentCoursesStmt = null;
        PreparedStatement deleteStudentStmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            int id = Integer.parseInt(studentId);

            // --- CRITICAL CHANGE: Get ALL relevant enrollment_ids directly from the 'enrollments' table ---
            // This ensures you capture all enrollments linked to the student before deleting their children.
            String getEnrollmentIdsFromEnrollmentsSql = "SELECT enrollment_id FROM enrollments WHERE student_id = ?";
            List<Integer> enrollmentIdsToDelete = new ArrayList<>();
            try(PreparedStatement ps = conn.prepareStatement(getEnrollmentIdsFromEnrollmentsSql)) {
                ps.setInt(1, id);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        enrollmentIdsToDelete.add(rs.getInt("enrollment_id"));
                    }
                }
            }
            System.out.println("DEBUG: Found " + enrollmentIdsToDelete.size() + " enrollment IDs for student " + id + " from 'enrollments' table.");


            // Proceed with deletions only if there are enrollments to process (and thus their children)
            if (!enrollmentIdsToDelete.isEmpty()) {
                String placeholders = String.join(",", java.util.Collections.nCopies(enrollmentIdsToDelete.size(), "?"));

                // 1. Delete from `marks` table (Lowest child level, assuming it links to enrollment_id)
                String deleteMarksSql = "DELETE FROM marks WHERE enrollment_id IN (" + placeholders + ")";
                deleteMarksStmt = conn.prepareStatement(deleteMarksSql);
                for (int i = 0; i < enrollmentIdsToDelete.size(); i++) {
                    deleteMarksStmt.setInt(i + 1, enrollmentIdsToDelete.get(i));
                }
                int marksDeleted = deleteMarksStmt.executeUpdate();
                System.out.println("DEBUG: Deleted " + marksDeleted + " marks records for student ID: " + id);

                // 2. Delete from `attendancerecords` table (Lowest child level, confirmed by error)
                // This MUST be done before deleting from 'enrollments'
                String deleteAttendanceSql = "DELETE FROM attendancerecords WHERE enrollment_id IN (" + placeholders + ")";
                deleteAttendanceStmt = conn.prepareStatement(deleteAttendanceSql);
                for (int i = 0; i < enrollmentIdsToDelete.size(); i++) {
                    deleteAttendanceStmt.setInt(i + 1, enrollmentIdsToDelete.get(i));
                }
                int attendanceDeleted = deleteAttendanceStmt.executeUpdate();
                System.out.println("DEBUG: Deleted " + attendanceDeleted + " attendance records for student ID: " + id);
            } else {
                System.out.println("DEBUG: No enrollments found for student " + id + ". Skipping marks and attendance deletion.");
            }


            // 3. Delete from `enrollments` table (Child of `students`, but parent of `marks` and `attendancerecords`)
            // This can now be deleted because its children (marks, attendance) have been handled
            String deleteEnrollmentsSql = "DELETE FROM enrollments WHERE student_id = ?";
            deleteEnrollmentsStmt = conn.prepareStatement(deleteEnrollmentsSql);
            deleteEnrollmentsStmt.setInt(1, id);
            int enrollmentsDeleted = deleteEnrollmentsStmt.executeUpdate();
            System.out.println("DEBUG: Deleted " + enrollmentsDeleted + " enrollment records for student ID: " + id);


            // 4. Delete from `student_courses` table (Often a mapping table, delete before `students`)
            String deleteStudentCoursesSql = "DELETE FROM student_courses WHERE student_id = ?";
            deleteStudentCoursesStmt = conn.prepareStatement(deleteStudentCoursesSql);
            deleteStudentCoursesStmt.setInt(1, id);
            int studentCoursesDeleted = deleteStudentCoursesStmt.executeUpdate();
            System.out.println("DEBUG: Deleted " + studentCoursesDeleted + " student_courses records for student ID: " + id);


            // 5. Delete from `students` table (The main parent record, always delete last)
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
            // Ensure all resources are closed, regardless of success or failure
            if (deleteMarksStmt != null) { try { deleteMarksStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteMarksStmt: " + e.getMessage()); } }
            if (deleteAttendanceStmt != null) { try { deleteAttendanceStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteAttendanceStmt: " + e.getMessage()); } }
            if (deleteEnrollmentsStmt != null) { try { deleteEnrollmentsStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteEnrollmentsStmt: " + e.getMessage()); } }
            if (deleteStudentCoursesStmt != null) { try { deleteStudentCoursesStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteStudentCoursesStmt: " + e.getMessage()); } }
            if (deleteStudentStmt != null) { try { deleteStudentStmt.close(); } catch (SQLException e) { System.err.println("Error closing deleteStudentStmt: " + e.getMessage()); } }
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /* log if needed */ } // Reset auto-commit
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

    // This private helper method was previously in UserDAO, now using ProgramCourseDAO
    // This method is redundant here as it's just delegating. If ProgramCourseDAO is a separate DAO,
    // then this method should be removed and ProgramCourseDAO.getCoursesByProgramAndSemester should be called directly.
    // It's fine to keep it if you prefer this encapsulation, but it's not strictly necessary.
    private List<Course> getCoursesByProgramAndSemester(int programId, int semester) throws SQLException {
        // Delegate to ProgramCourseDAO
        return programCourseDAO.getCoursesByProgramAndSemester(programId, semester);
    }
}