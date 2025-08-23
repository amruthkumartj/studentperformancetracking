// src/main/java/com/portal/PerformanceDAO.java
package com.portal.datatransfer_access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap; // Added for getPrograms/getProgramSemesters
import java.util.List;
import java.util.Map;   // Added for getPrograms/getProgramSemesters

import com.portal.CoursePerformance;
import com.portal.DBUtil;
import com.portal.Student;
import com.portal.StudentPerformance;

public class PerformanceDAO {

    // No longer need MarksDAO or AttendanceDAO here, as detailed student performance
    // will be handled by UserDAO.
    // private MarksDAO marksDAO;
    // private AttendanceDAO attendanceDAO;

    public PerformanceDAO() {
        // If you had any specific initialization for this DAO alone, keep it.
        // Otherwise, these are not needed if UserDAO handles detailed performance.
        // this.marksDAO = new MarksDAO();
        // this.attendanceDAO = new AttendanceDAO();
    }
    /**
     * Retrieves a complete performance profile for a student using only their ID.
     * This is the primary method for the AI Assistant.
     * @param studentId The ID of the student.
     * @return A StudentPerformance object populated with all course data, or null if not found.
     * @throws SQLException If a database access error occurs.
     */
    public StudentPerformance getStudentPerformanceById(int studentId) throws SQLException {
        StudentPerformance studentPerf = null;
        // This query uses LEFT JOINs and aggregation to get student details,
        // sum up attendance, and pivot marks into columns for each course.
        String sql = "SELECT " +
                     "s.student_id, s.student_name, p.program_name, s.sem, c.course_id, c.course_name, " +
                     "SUM(CASE WHEN ar.attendance_status = 'PRESENT' THEN 1 ELSE 0 END) as classes_attended, " +
                     "COUNT(DISTINCT ass.session_id) as total_classes_held, " +
                     "MAX(CASE WHEN m.exam_type = 'Internal Assessment 1' THEN m.marks_obtained END) as ia1_marks, " +
                     "MAX(CASE WHEN m.exam_type = 'Internal Assessment 2' THEN m.marks_obtained END) as ia2_marks, " +
                     "MAX(CASE WHEN m.exam_type = 'SEE (Semester End Examination)' THEN m.marks_obtained END) as see_marks " +
                     "FROM students s " +
                     "JOIN enrollments e ON s.student_id = e.student_id " +
                     "JOIN programs p ON e.program_id = p.program_id " +
                     "LEFT JOIN student_courses sc ON s.student_id = sc.student_id " +
                     "LEFT JOIN courses c ON sc.course_id = c.course_id " +
                     "LEFT JOIN attendancesessions ass ON c.course_id = ass.course_id AND ass.semester = s.sem AND ass.status = 'COMPLETED' " +
                     "LEFT JOIN attendancerecords ar ON ass.session_id = ar.session_id AND ar.student_id = s.student_id " +
                     "LEFT JOIN marks m ON sc.enrollment_id = m.enrollment_id AND c.course_id = m.course_id " +
                     "WHERE s.student_id = ? " +
                     "GROUP BY s.student_id, s.student_name, p.program_name, s.sem, c.course_id, c.course_name " +
                     "ORDER BY c.course_name;";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            
            try (ResultSet rs = ps.executeQuery()) {
                List<CoursePerformance> courses = new ArrayList<>();
                boolean studentFound = false;

                while (rs.next()) {
                    if (!studentFound) {
                        // This part runs only once to set up the main student object
                        studentPerf = new StudentPerformance();
                        studentPerf.setStudentId(studentId);
                        studentPerf.setStudentName(rs.getString("student_name"));
                        studentPerf.setProgramName(rs.getString("program_name"));
                        studentPerf.setSemester(rs.getInt("sem"));
                        studentFound = true;
                    }

                    // Use the constructor from your CoursePerformance DTO
                    CoursePerformance coursePerf = new CoursePerformance(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getObject("ia1_marks") != null ? rs.getDouble("ia1_marks") : null,
                        rs.getObject("ia2_marks") != null ? rs.getDouble("ia2_marks") : null,
                        rs.getObject("see_marks") != null ? rs.getDouble("see_marks") : null,
                        rs.getInt("total_classes_held"),
                        rs.getInt("classes_attended")
                    );
                    
                    courses.add(coursePerf);
                }

                if (!studentFound) {
                    return null; // Return null if the student ID doesn't exist at all
                }

                studentPerf.setCoursePerformances(courses);
                // The overall analysis can be calculated here or in the DTO if needed
            }
        }
        return studentPerf;
    }

    /**
     * Retrieves a list of students based on programId and semester.
     * This is used for populating the student list table in the performance view (Overall Performance).
     *
     * @param programId The ID of the program.
     * @param semester The semester number.
     * @return A list of Student objects, or an empty list if none found.
     * @throws SQLException if a database access error occurs.
     */
    public List<Student> getStudentsByProgramAndSemester(int programId, int semester) throws SQLException {
        List<Student> students = new ArrayList<>();
        // Corrected SQL based on your provided query and previous discussions:
        // Joining 'enrollments' and 'programs' to get program_name and use program_id from enrollments
        String sql = "SELECT s.student_id, s.student_name, p.program_name, s.sem, s.phone, s.email " +
                     "FROM students s " +
                     "JOIN enrollments e ON s.student_id = e.student_id " +
                     "JOIN programs p ON e.program_id = p.program_id " +
                     "WHERE e.program_id = ? AND s.sem = ?"; // Use s.sem (student's current semester)

        System.out.println("DEBUG DAO: getStudentsByProgramAndSemester - SQL: " + sql);
        System.out.println("DEBUG DAO: getStudentsByProgramAndSemester - Params: programId=" + programId + ", semester=" + semester);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, programId);
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("student_id"));
                    student.setFullName(rs.getString("student_name")); // Assuming student_name is the full name
                    // Populate Program ID from the method parameter, not from DB unless needed specifically
                    // student.setProgramId(rs.getInt("program_id")); // If program_id is selected, can use this
                    student.setProgramId(programId); // Use the parameter passed
                    student.setProgramName(rs.getString("program_name")); // Get from JOINed table
                    student.setSemester(rs.getInt("sem")); // Get from student table (s.sem)
                    student.setPhone(rs.getString("phone"));
                    student.setEmail(rs.getString("email"));
                    students.add(student);
                }
            }
        }
        System.out.println("DEBUG DAO: getStudentsByProgramAndSemester - Found " + students.size() + " students.");
        return students;
    }

    /**
     * Retrieves all available programs for dropdowns.
     * @return A list of maps, each containing "id" and "name" of a program.
     * @throws SQLException if a database access error occurs.
     */
    public List<Map<String, Object>> getPrograms() throws SQLException {
        List<Map<String, Object>> programs = new ArrayList<>();
        String sql = "SELECT program_id, program_name FROM programs";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> program = new HashMap<>();
                program.put("id", rs.getInt("program_id"));
                program.put("name", rs.getString("program_name"));
                programs.add(program);
            }
        }
        return programs;
    }

    /**
     * Retrieves all available semesters for a specific program.
     * Assumes 'enrollments' or 'student_programs' table has distinct semesters per program.
     * @param programId The ID of the program.
     * @return A list of unique semester numbers for that program.
     * @throws SQLException if a database access error occurs.
     */
    public List<Integer> getProgramSemesters(int programId) throws SQLException {
        List<Integer> semesters = new ArrayList<>();
        // Assuming 'enrollments' table contains program_id and semester (sem column in students table, related to enrollment)
        // Or if you have a student_courses / student_programs table with semester.
        // Based on your getStudentsByProgramAndSemester, 's.sem' seems to be the semester.
        // It's more logical to get distinct semesters from the `enrollments` table based on `program_id`.
        String sql = "SELECT DISTINCT e.semester_number FROM enrollments e WHERE e.program_id = ? ORDER BY e.semester_number";
        // If your students table has `sem` directly linked to their current semester in program:
        // String sql = "SELECT DISTINCT s.sem FROM students s JOIN enrollments e ON s.student_id = e.student_id WHERE e.program_id = ? ORDER BY s.sem";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, programId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    semesters.add(rs.getInt("semester_number")); // Or "sem" if using the alternative query
                }
            }
        }
        return semesters;
    }
}