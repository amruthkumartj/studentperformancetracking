package com.portal.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// --- Vertex AI Imports ---
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

// --- DTO and DAO Imports from your Project ---
import com.portal.Event;
import com.portal.Schedule;
import com.portal.Student;
import com.portal.StudentPerformance;
import com.portal.User;
import com.portal.datatransfer_access.EventsDAO;
import com.portal.datatransfer_access.FacultyDAO;
import com.portal.datatransfer_access.PerformanceDAO;
import com.portal.datatransfer_access.ProgramCourseDAO;
import com.portal.datatransfer_access.ScheduleDAO;
import com.portal.datatransfer_access.UserDAO;


@WebServlet("/AssistantServlet")
public class AssistantServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // --- AI Model Configuration ---
    private static final String PROJECT_ID = System.getenv("PROJECT_ID");
    private static final String LOCATION = System.getenv("LOCATION");
    private static final String MODEL_NAME = System.getenv("MODEL_NAME");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Gson gson = new Gson();
        JsonObject jsonPayload = gson.fromJson(requestBody, JsonObject.class);
        String userQuestion = jsonPayload.get("question").getAsString().toLowerCase();

        // --- Securely Get User Info from Session ---
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated.");
            return;
        }
        User user = (User) session.getAttribute("user");

        String contextData = "";
        String securityNote = "";
        boolean isPortalQuery = false;

        try {
            // --- DAO Instances ---
            FacultyDAO facultyDAO = new FacultyDAO();
            Integer facultyId = -1;

            if ("FACULTY".equalsIgnoreCase(user.getRole())) {
                facultyId = facultyDAO.getFacultyIdByUserId(user.getId());
            }

            // --- INTENT DETECTION & DATA FETCHING ---
            StringBuilder combinedContext = new StringBuilder();

            // Check for profile/program related queries
            if (userQuestion.contains("my program") || userQuestion.contains("assigned to") || (userQuestion.contains("how many") && userQuestion.contains("program")) || userQuestion.contains("my name") || userQuestion.contains("my role")) {
                isPortalQuery = true;
                List<String[]> programs;
                boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
                if (isAdmin) {
                    ProgramCourseDAO pcDAO = new ProgramCourseDAO();
                    programs = pcDAO.getAllPrograms();
                } else {
                    if (facultyId != -1) {
                        programs = facultyDAO.getAssignedPrograms(facultyId);
                    } else {
                        programs = new ArrayList<>();
                    }
                }
                combinedContext.append(buildProfileAndProgramsContext(user, programs, isAdmin)).append(" ");
            }

            // Check for schedule/event related queries
            if (userQuestion.contains("schedule") || userQuestion.contains("event") || userQuestion.contains("ongoing") || userQuestion.contains("upcoming") || userQuestion.contains("hosts")) {
                isPortalQuery = true;
                combinedContext.append(handleScheduleAndEventQueries(userQuestion, facultyId, facultyDAO)).append(" ");
            }
            
            // Check for student related queries
            if (userQuestion.contains("student")) {
                 isPortalQuery = true;
                 combinedContext.append(handleStudentQueries(userQuestion, user, facultyId, facultyDAO, securityNote)).append(" ");
            }

            // Fallback for specific student ID query if no other intent matched
            Integer studentId = extractStudentId(userQuestion);
            if (!isPortalQuery && studentId != null) {
                isPortalQuery = true;
                PerformanceDAO performanceDAO = new PerformanceDAO();
                StudentPerformance studentPerf = performanceDAO.getStudentPerformanceById(studentId);
                
                if (studentPerf != null) {
                    if (isUserAuthorizedForProgram(user, facultyDAO, facultyId, studentPerf.getProgramName())) {
                        combinedContext.append(buildStudentPerformanceContext(studentPerf));
                    } else {
                        combinedContext.append("Access Denied.");
                        securityNote = "You are not authorized to view data for this program.";
                    }
                } else {
                    combinedContext.append("No student found with ID: ").append(studentId);
                }
            }

            contextData = combinedContext.toString().trim();

        } catch (SQLException e) {
            e.printStackTrace();
            contextData = "An error occurred while accessing the database.";
        } catch (Exception e) {
            e.printStackTrace();
            contextData = "An unexpected application error occurred.";
        }

        // --- Construct the Final Prompt for the AI ---
        String finalPrompt;
        if (isPortalQuery && !contextData.isEmpty()) {
            finalPrompt = "You are a helpful faculty assistant for a college portal. "
                       + "Answer the user's question based ONLY on the provided context. "
                       + "If the context is empty or does not contain the answer, say so. "
                       + "You MUST strictly follow any security notes. "
                       + "If the context is 'Access Denied', you MUST inform the user they lack permission and explain why based on the security note. "
                       + "Be concise, clear, and present data in a readable way.\n\n"
                       + "--- Security Note ---\n"
                       + securityNote + "\n"
                       + "--- Provided Context ---\n"
                       + contextData + "\n"
                       + "--- End Context ---\n\n"
                       + "User Question: " + userQuestion;
        } else {
            finalPrompt = userQuestion;
        }

        // --- Call the Gemini API ---
        String geminiResponse;
        try (VertexAI vertexAi = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAi);
            GenerateContentResponse apiResponse = model.generateContent(finalPrompt);
            geminiResponse = ResponseHandler.getText(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            geminiResponse = "Sorry, I encountered an error while contacting the AI model.";
        }

        // --- Send Response to Frontend ---
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(geminiResponse);
    }

    // --- Handler Methods for Different Intents ---

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
        if (userQuestion.contains("event") || userQuestion.contains("upcoming") || userQuestion.contains("ongoing") || userQuestion.contains("hosts")) {
            EventsDAO eventsDAO = new EventsDAO();
            List<Event> upcomingEvents = eventsDAO.getUpcomingEvents();
            combinedContext.append(buildEventsContext(upcomingEvents));
        }
        return combinedContext.toString().trim();
    }

    private String handleStudentQueries(String userQuestion, User user, int facultyId, FacultyDAO facultyDAO, String securityNote) throws SQLException {
        String programName = extractProgramName(userQuestion);
        if (programName == null) return ""; 

        if (!isUserAuthorizedForProgram(user, facultyDAO, facultyId, programName)) {
            securityNote = "You are not authorized to view data for this program.";
            return "Access Denied.";
        }

        ProgramCourseDAO pcDAO = new ProgramCourseDAO();
        UserDAO userDAO = new UserDAO();
        int programId = getProgramIdByAcronym(pcDAO, programName);
        if (programId == -1) return "Could not find a program named '" + programName + "'.";

        Integer semester = extractSemester(userQuestion);

        if (userQuestion.contains("details")) {
            List<Student> students = new ArrayList<>();
            if (semester != null) {
                students = userDAO.getStudentsByProgramAndSemester(programId, semester);
            } else {
                // Fetch for all semesters if no specific one is mentioned
                List<Integer> allSemesters = pcDAO.getDistinctSemestersByProgram(programId);
                for(int sem : allSemesters) {
                    students.addAll(userDAO.getStudentsByProgramAndSemester(programId, sem));
                }
            }
            return buildStudentListContext(students, programName);
        }
        
        if (userQuestion.contains("how many")) {
            if (semester != null) {
                List<Student> students = userDAO.getStudentsByProgramAndSemester(programId, semester);
                return "There are " + students.size() + " students in " + programName + " Semester " + semester + ".";
            } else {
                int count = pcDAO.getTotalStudentsInProgram(programId);
                return "There are " + count + " students in the " + programName + " program in total.";
            }
        }

        if (userQuestion.contains("lowest attendance")) {
            if (semester == null) return "Please specify a semester to find the student with the lowest attendance.";
            
            PerformanceDAO performanceDAO = new PerformanceDAO();
            List<Student> students = userDAO.getStudentsByProgramAndSemester(programId, semester);
            StudentPerformance lowestStudent = null;
            double minAttendance = 101.0;

            for (Student student : students) {
                try {
                    StudentPerformance perf = performanceDAO.getStudentPerformanceById(student.getStudentId());
                    if (perf != null && perf.getOverallAttendancePercentage() < minAttendance) {
                        minAttendance = perf.getOverallAttendancePercentage();
                        lowestStudent = perf;
                    }
                } catch (Exception e) {
                    System.err.println("Could not get performance for student ID " + student.getStudentId() + ": " + e.getMessage());
                }
            }
            return buildLowestAttendanceContext(lowestStudent, programName, semester);
        }
        return "";
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
        if (students == null || students.isEmpty()) return "No students found for the " + programName + " program.";
        StringBuilder sb = new StringBuilder("There are a total of " + students.size() + " students. Here are their details for the " + programName + " program: ");
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
            sb.append("Event: '").append(event.getEventName()).append("'. ");
            sb.append("Date: ").append(event.getEventDate()).append(". ");
            sb.append("Category: ").append(event.getEventCategory()).append(". ");
            sb.append("Registration ends on: ").append(event.getRegistrationEndDate()).append(". ");
            sb.append("Link: ").append(event.getEventLink()).append(". ");
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
        
        if (isAdmin) {
            sb.append("As an admin, you can see all programs: ");
        } else {
            sb.append("You are currently assigned to the following programs: ");
        }
        
        programs.forEach(p -> sb.append(p[1]).append(" (ID: ").append(p[0]).append("), "));
        return sb.substring(0, sb.length() - 2) + ".";
    }
    
    private String buildScheduleContext(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) return "You have no extra schedules planned from today onwards.";
        StringBuilder sb = new StringBuilder("Here are your upcoming extra schedules: ");
        schedules.forEach(s -> {
            sb.append("On ").append(s.getClassDate()).append(" at ").append(s.getClassTime()).append(", ");
            sb.append("you have '").append(s.getSubjectName()).append("' ");
            sb.append("at location '").append(s.getLocation()).append("'. ");
        });
        return sb.toString();
    }

    private String buildLowestAttendanceContext(StudentPerformance student, String programName, int semester) {
        if (student != null) {
            return "The student with the lowest attendance is " + student.getStudentName() + 
                   " (ID: " + student.getStudentId() + ") with an overall attendance of " + 
                   String.format("%.2f%%", student.getOverallAttendancePercentage()) + ".";
        } else {
            return "Could not determine the student with the lowest attendance for " + programName + " semester " + semester + ".";
        }
    }

    // --- Helper Methods for Logic and Parsing ---

    private boolean isUserAuthorizedForProgram(User user, FacultyDAO facultyDAO, int facultyId, String programAcronym) throws SQLException {
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            return true;
        }
        if (facultyId == -1) return false;
        
        List<String[]> assignedPrograms = facultyDAO.getAssignedPrograms(facultyId);
        for (String[] program : assignedPrograms) {
            if (program[1].toUpperCase().contains("(" + programAcronym.toUpperCase() + ")")) {
                return true;
            }
        }
        return false;
    }

    private int getProgramIdByAcronym(ProgramCourseDAO pcDAO, String programAcronym) throws SQLException {
        List<String[]> allPrograms = pcDAO.getAllPrograms();
        for (String[] p : allPrograms) {
            if (p[1].toUpperCase().contains("(" + programAcronym.toUpperCase() + ")")) {
                return Integer.parseInt(p[0]);
            }
        }
        return -1; // Not found
    }

    private Integer extractStudentId(String question) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(question);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        return null;
    }
    
    private String extractProgramName(String question) {
        Pattern pattern = Pattern.compile("(mca|mba|bca|b\\.tech|m\\.tech)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(question);
        if (matcher.find()) {
            return matcher.group(0).toUpperCase();
        }
        return null;
    }

    private Integer extractSemester(String question) {
        // This regex now handles "sem 2" as well as "2nd sem"
        Pattern pattern = Pattern.compile("(?:semester|sem)\\s*(\\d+)|(\\d+)(?:st|nd|rd|th)?\\s*(?:semester|sem)");
        Matcher matcher = pattern.matcher(question);
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                return Integer.parseInt(matcher.group(1));
            } else if (matcher.group(2) != null) {
                return Integer.parseInt(matcher.group(2));
            }
        }
        return null;
    }
}
