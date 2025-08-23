package com.portal.datatransfer_access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.portal.DBUtil;
import com.portal.Event;
import com.portal.Schedule;
import com.portal.Student;
import com.portal.StudentPerformance;
import com.portal.User;

/**
 * AI Data Access Object
 * This class encapsulates all the logic for processing a user's question,
 * fetching the relevant data, and building a context string for the AI model.
 */
public class AIDAO {

    // --- Main public method to be called by the servlet ---
    
    public String getContextForQuery(String userQuestion, User user) throws SQLException {
        
        FacultyDAO facultyDAO = new FacultyDAO();
        int facultyId = -1;
        if ("FACULTY".equalsIgnoreCase(user.getRole())) {
            facultyId = facultyDAO.getFacultyIdByUserId(user.getId());
        }

        // --- INTENT ROUTING ---
        StringBuilder combinedContext = new StringBuilder();

        if (userQuestion.contains("student") || extractStudentId(userQuestion) != null) {
            combinedContext.append(handleStudentQueries(userQuestion, user, facultyId, facultyDAO)).append(" ");
        }
        if (userQuestion.contains("schedule") || userQuestion.contains("event") || userQuestion.contains("upcoming") || userQuestion.contains("ongoing")) {
            combinedContext.append(handleScheduleAndEventQueries(userQuestion, facultyId, facultyDAO)).append(" ");
        }
        if (userQuestion.contains("my program") || userQuestion.contains("assigned to") || userQuestion.contains("my name") || userQuestion.contains("my role")) {
            combinedContext.append(handleProfileQueries(user, facultyDAO)).append(" ");
        }
        
        return combinedContext.toString().trim();
    }

    // --- Private Handler Methods for Different Intents ---

    private String handleStudentQueries(String userQuestion, User user, int facultyId, FacultyDAO facultyDAO) throws SQLException {
        Integer studentId = extractStudentId(userQuestion);
        String programName = extractProgramName(userQuestion);
        
        UserDAO userDAO = new UserDAO(); 
        PerformanceDAO performanceDAO = new PerformanceDAO();
        ProgramCourseDAO pcDAO = new ProgramCourseDAO();

        // INTENT 1: Get performance for a specific student ID
        if (studentId != null) {
            Student student = getStudentById(studentId); 
            
            if (student == null) {
                return "No student found with ID: " + studentId;
            }
            String actualProgramAcronym = getAcronymFromProgramName(student.getProgramName());
            if (programName != null && !programName.equalsIgnoreCase(actualProgramAcronym)) {
                return "Student with ID " + studentId + " is in the " + actualProgramAcronym + " program, not " + programName.toUpperCase() + ".";
            }
            if (!isUserAuthorizedForProgram(user, facultyDAO, facultyId, actualProgramAcronym)) {
                return "Access Denied.";
            }
            StudentPerformance studentPerf = performanceDAO.getStudentPerformanceById(studentId);
            return (studentPerf != null) ? buildStudentPerformanceContext(studentPerf) : "Could not fetch performance details for student " + student.getFullName() + ".";
        }

        // INTENT 2: Handle list-based queries
        if (programName != null) {
            if (!isUserAuthorizedForProgram(user, facultyDAO, facultyId, programName)) {
                return "Access Denied.";
            }
            int programId = getProgramIdByAcronym(pcDAO, programName);
            if (programId == -1) return "Could not find a program named '" + programName + "'.";
            
            Integer semester = extractSemester(userQuestion);
            
            if (userQuestion.contains("details")) {
                List<Student> students = new ArrayList<>();
                if (semester != null) {
                    students = userDAO.getStudentsByProgramAndSemester(programId, semester);
                } else {
                    List<Integer> allSemesters = pcDAO.getDistinctSemestersByProgram(programId);
                    for(int sem : allSemesters) {
                        students.addAll(userDAO.getStudentsByProgramAndSemester(programId, sem));
                    }
                }
                return buildStudentListContext(students, programName);
            }
            
            if (userQuestion.contains("how many")) {
                int count = (semester != null) ? userDAO.getStudentsByProgramAndSemester(programId, semester).size() : pcDAO.getTotalStudentsInProgram(programId);
                return "There are " + count + " students in " + programName.toUpperCase() + (semester != null ? " Semester " + semester : " in total") + ".";
            }
        }
        return "";
    }

    private String handleScheduleAndEventQueries(String userQuestion, int facultyId, FacultyDAO facultyDAO) throws SQLException {
        StringBuilder combinedContext = new StringBuilder();
        if (userQuestion.contains("schedule") || userQuestion.contains("extra class")) {
            if (facultyId != -1) {
                ScheduleDAO scheduleDAO = new ScheduleDAO();
                ProgramCourseDAO pcDAO = new ProgramCourseDAO();
                List<Schedule> allSchedules = new ArrayList<>();
                List<String[]> assignedPrograms = facultyDAO.getAssignedPrograms(facultyId);
                for (String[] program : assignedPrograms) {
                    int programId = Integer.parseInt(program[0]);
                    List<Integer> semesters = pcDAO.getDistinctSemestersByProgram(programId);
                    for (Integer semester : semesters) {
                        allSchedules.addAll(scheduleDAO.getAllExtraSchedules(programId, semester));
                    }
                }
                List<Schedule> mySchedules = allSchedules.stream()
                    .filter(s -> s.getFacultyId() == facultyId && !s.getClassDate().isBefore(LocalDate.now()))
                    .collect(Collectors.toList());
                combinedContext.append(buildScheduleContext(mySchedules)).append(" ");
            }
        }
        if (userQuestion.contains("event") || userQuestion.contains("upcoming") || userQuestion.contains("ongoing")) {
            EventsDAO eventsDAO = new EventsDAO();
            List<Event> upcomingEvents = eventsDAO.getUpcomingEvents();
            combinedContext.append(buildEventsContext(upcomingEvents));
        }
        return combinedContext.toString().trim();
    }

    private String handleProfileQueries(User user, FacultyDAO facultyDAO) throws SQLException {
        ProgramCourseDAO pcDAO = new ProgramCourseDAO();
        int facultyId = "FACULTY".equalsIgnoreCase(user.getRole()) ? facultyDAO.getFacultyIdByUserId(user.getId()) : -1;
        
        List<String[]> programs = new ArrayList<>();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (isAdmin) {
            programs = pcDAO.getAllPrograms();
        } else if (facultyId != -1) {
            programs = facultyDAO.getAssignedPrograms(facultyId);
        }
        
        return buildProfileAndProgramsContext(user, programs, isAdmin);
    }
    
    /**
     * ✅ FIX: This method has been rewritten to use two simple queries instead of one complex JOIN.
     * This is more robust and avoids potential JOIN-related SQL errors.
     */
    private Student getStudentById(int studentId) throws SQLException {
        Student student = null;
        String programName = null;
        int programId = -1;

        // Step 1: Get basic student info from the 'students' table first.
        String studentSql = "SELECT student_name, email, phone, sem, program_id FROM students WHERE student_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(studentSql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    student = new Student();
                    student.setStudentId(studentId);
                    student.setFullName(rs.getString("student_name")); 
                    student.setEmail(rs.getString("email"));
                    student.setPhone(rs.getString("phone"));
                    student.setSemester(rs.getInt("sem"));
                    programId = rs.getInt("program_id"); // Get the program_id for the next query
                } else {
                    return null; // Student not found, exit early.
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in AIDAO.getStudentById (Step 1: fetching student) for ID " + studentId + ": " + e.getMessage());
            throw e;
        }

        // Step 2: If student was found, get the program name from the 'programs' table.
        if (student != null && programId != -1) {
            String programSql = "SELECT program_name FROM programs WHERE id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(programSql)) {
                
                pstmt.setInt(1, programId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        programName = rs.getString("program_name");
                        student.setProgramName(programName);
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQL Error in AIDAO.getStudentById (Step 2: fetching program name) for program_id " + programId + ": " + e.getMessage());
                throw e;
            }
        }
        
        return student;
    }

    // --- Helper Methods for Context Building ---

    private String buildStudentPerformanceContext(StudentPerformance perf) {
        if (perf == null) return "No student data found.";
        StringBuilder sb = new StringBuilder();
        sb.append("Performance summary for student ").append(perf.getStudentName()).append(" (ID: ").append(perf.getStudentId()).append("), ");
        sb.append(perf.getProgramName()).append(" - Semester ").append(perf.getSemester()).append(". ");
        sb.append("Overall Attendance: ").append(String.format("%.2f%%", perf.getOverallAttendancePercentage())).append(". ");
        sb.append("Course Details: ");
        perf.getCoursePerformances().forEach(cp -> {
            sb.append(cp.getSubjectName()).append(": ");
            sb.append("Attendance: ").append(String.format("%.2f%%", cp.getAttendancePercentage())).append(". ");
            sb.append("IA1 Marks: ").append(cp.getIa1Marks() != null ? cp.getIa1Marks() : "N/A").append(". ");
            sb.append("IA2 Marks: ").append(cp.getIa2Marks() != null ? cp.getIa2Marks() : "N/A").append(". ");
            sb.append("SEE Marks: ").append(cp.getSeeMarks() != null ? cp.getSeeMarks() : "N/A").append(". ");
        });
        return sb.toString();
    }
     
    private String buildStudentListContext(List<Student> students, String programName) {
        if (students == null || students.isEmpty()) return "No students found for the " + programName.toUpperCase() + " program.";
        StringBuilder sb = new StringBuilder("There are a total of " + students.size() + " students. Here are their details for the " + programName.toUpperCase() + " program: ");
        
        students.forEach(s -> {
            sb.append("Name: ").append(s.getFullName()).append(", ");
            sb.append("ID: ").append(s.getStudentId()).append(", ");
            sb.append("Semester: ").append(s.getSemester()).append(", ");
            sb.append("Email: ").append(s.getEmail()).append(", ");
            sb.append("Phone: ").append(s.getPhone()).append(". ");
        });
        return sb.toString();
    }

    private String buildEventsContext(List<Event> events) {
        if (events == null || events.isEmpty()) return "There are no upcoming events.";
        StringBuilder sb = new StringBuilder("Here are the upcoming events: ");
        events.forEach(event -> {
            sb.append("Event: '").append(event.getEventName()).append("', ");
            sb.append("Date: ").append(event.getEventDate()).append(". ");
        });
        return sb.toString();
    }

    private String buildProfileAndProgramsContext(User user, List<String[]> programs, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("Your name is ").append(user.getUsername()).append(" and your role is ").append(user.getRole()).append(". ");
        if (programs == null || programs.isEmpty()) {
            sb.append(isAdmin ? "There are no programs in the system." : "You are not assigned to any programs.");
            return sb.toString();
        }
        sb.append(isAdmin ? "As an admin, you can see all programs: " : "You are currently assigned to the following programs: ");
        programs.forEach(p -> sb.append(p[1]).append(" (ID: ").append(p[0]).append("), "));
        return sb.substring(0, sb.length() - 2) + ".";
    }
     
    private String buildScheduleContext(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) return "You have no extra schedules planned from today onwards.";
        StringBuilder sb = new StringBuilder("Here are your upcoming extra schedules: ");
        schedules.forEach(s -> {
            sb.append("On ").append(s.getClassDate()).append(" at ").append(s.getClassTime()).append(", ");
            sb.append("you have '").append(s.getSubjectName()).append("' at '").append(s.getLocation()).append("'. ");
        });
        return sb.toString();
    }

    // --- Helper Methods for Logic and Parsing ---

    private boolean isUserAuthorizedForProgram(User user, FacultyDAO facultyDAO, int facultyId, String programAcronym) throws SQLException {
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) return true;
        if (facultyId == -1) return false;
        return facultyDAO.getAssignedPrograms(facultyId).stream().anyMatch(p -> p[1].toUpperCase().contains("(" + programAcronym.toUpperCase() + ")"));
    }

    private int getProgramIdByAcronym(ProgramCourseDAO pcDAO, String programAcronym) throws SQLException {
        return pcDAO.getAllPrograms().stream()
            .filter(p -> p[1].toUpperCase().contains("(" + programAcronym.toUpperCase() + ")"))
            .map(p -> Integer.parseInt(p[0]))
            .findFirst()
            .orElse(-1);
    }

    private Integer extractStudentId(String question) {
        Pattern pattern = Pattern.compile("student(?: id| number)?\\s*(\\d+)|\\b(\\d{2,})\\b");
        Matcher matcher = pattern.matcher(question);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        }
        return null;
    }
     
    private String extractProgramName(String question) {
        Pattern pattern = Pattern.compile("(mca|mba|bca|b\\.tech|m\\.tech)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(question);
        return matcher.find() ? matcher.group(0).toUpperCase() : null;
    }

    private Integer extractSemester(String question) {
        Pattern pattern = Pattern.compile("(?:semester|sem)\\s*(\\d+)|(\\d+)(?:st|nd|rd|th)?\\s*(?:semester|sem)");
        Matcher matcher = pattern.matcher(question);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        }
        return null;
    }
    
    private String getAcronymFromProgramName(String fullProgramName) {
        if (fullProgramName == null) return "";
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(fullProgramName);
        return matcher.find() ? matcher.group(1) : fullProgramName;
    }
}
