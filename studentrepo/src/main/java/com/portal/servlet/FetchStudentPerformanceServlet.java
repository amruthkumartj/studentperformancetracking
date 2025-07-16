package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.portal.PerformanceDAO; // For student lists and program/semester info
import com.portal.UserDAO;      // For specific student detailed performance
import com.portal.StudentPerformance; // DTO for detailed performance
import com.portal.Student; // DTO for student list

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/FetchStudentPerformanceServlet")
public class FetchStudentPerformanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO; // Will handle detailed student performance
    private PerformanceDAO performanceDAO; // Will handle student lists and program/semester fetches
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        performanceDAO = new PerformanceDAO(); // Initialize the PerformanceDAO as well
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String jsonString = sb.toString();

        int programId = -1;
        int semester = -1;
        Integer studentId = null; // Use Integer to allow null for optional studentId
        String examType = null;   // For specific student performance view

        try {
            JsonObject requestData = gson.fromJson(jsonString, JsonObject.class);

            if (requestData.has("programId") && !requestData.get("programId").isJsonNull()) {
                try {
                    programId = requestData.get("programId").getAsInt();
                } catch (NumberFormatException e) {
                    System.err.println("ERROR Servlet: Invalid programId format: " + requestData.get("programId").getAsString());
                }
            }
            if (requestData.has("semester") && !requestData.get("semester").isJsonNull()) {
                try {
                    semester = requestData.get("semester").getAsInt();
                } catch (NumberFormatException e) {
                    System.err.println("ERROR Servlet: Invalid semester format: " + requestData.get("semester").getAsString());
                }
            }
            if (requestData.has("studentId") && !requestData.get("studentId").isJsonNull() && !requestData.get("studentId").getAsString().isEmpty()) {
                try {
                    studentId = requestData.get("studentId").getAsInt();
                } catch (NumberFormatException e) {
                    System.err.println("ERROR Servlet: Invalid studentId format: " + requestData.get("studentId").getAsString());
                }
            }
            // Parse examType
            if (requestData.has("examType") && !requestData.get("examType").isJsonNull()) {
                examType = requestData.get("examType").getAsString();
            }

            System.out.println("DEBUG Servlet: Received performance request - Raw JSON: " + jsonString);
            System.out.println("DEBUG Servlet: Parsed values - ProgramId: " + programId + ", Semester: " + semester + ", StudentId: " + (studentId != null ? studentId : "N/A") + ", ExamType: " + (examType != null ? examType : "N/A"));

            if (programId == -1 || semester == -1) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Program and Semester are required and must be valid numbers.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                if (studentId != null) {
                    // For specific student, examType is required
                    if (examType == null || examType.isEmpty()) {
                         jsonResponse.addProperty("status", "error");
                         jsonResponse.addProperty("message", "Exam Type is required for specific student performance.");
                         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        // Call UserDAO for detailed student performance
                        StudentPerformance studentPerformance = userDAO.getStudentPerformance(studentId, programId, semester, examType); // Call UserDAO
                        if (studentPerformance != null) {
                            jsonResponse.addProperty("status", "success");
                            jsonResponse.add("student", gson.toJsonTree(studentPerformance));
                            System.out.println("DEBUG Servlet: Sent specific student performance for ID: " + studentId);
                        } else {
                            jsonResponse.addProperty("status", "info");
                            jsonResponse.addProperty("message", "Student performance data not found for ID: " + studentId + " in selected program/semester.");
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            System.out.println("DEBUG Servlet: Student performance not found for ID: " + studentId);
                        }
                    }
                } else {
                    // Call PerformanceDAO for list of students
                    List<Student> students = performanceDAO.getStudentsByProgramAndSemester(programId, semester); // Call PerformanceDAO
                    if (students != null && !students.isEmpty()) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("students", gson.toJsonTree(students));
                        System.out.println("DEBUG Servlet: Sent list of " + students.size() + " students for program " + programId + " semester " + semester);
                    } else {
                        jsonResponse.addProperty("status", "info");
                        jsonResponse.addProperty("message", "No students found for Program ID: " + programId + ", Semester: " + semester);
                        System.out.println("DEBUG Servlet: No students found for program " + programId + " semester " + semester);
                    }
                }
            }

        } catch (JsonSyntaxException e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Invalid JSON format in request: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            System.err.println("ERROR Servlet: JSON syntax error: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("ERROR Servlet: SQL exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "An unexpected error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("ERROR Servlet: Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(jsonResponse));
            out.flush();
            System.out.println("DEBUG Servlet: Final Response JSON: " + gson.toJson(jsonResponse));
        }
    }
}