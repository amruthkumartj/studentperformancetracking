// src/main/java/com/portal/servlet/GetStudentsServlet.java
package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException; // Import for JsonSyntaxException
import com.portal.AttendanceDAO;
import com.portal.DBUtil; // Keep this import as DAOs use it
import com.portal.Student;
import com.portal.User;
import com.portal.UserDAO; // Import UserDAO

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection; // Keep import for DBUtil.getConnection() if used elsewhere, but not for DAO constructor directly
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors; // For request body collection

@WebServlet("/GetStudentsServlet")
public class GetStudentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();
    private AttendanceDAO attendanceDAO; // Declare DAO instances
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize DAOs here, they no longer need a Connection in their constructor
        attendanceDAO = new AttendanceDAO();
        userDAO = new UserDAO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User loggedInUser = null;
        if (session != null) {
            loggedInUser = (User) session.getAttribute("user");
        }

        // Authentication and Authorization check
        if (loggedInUser == null || (!"faculty".equalsIgnoreCase(loggedInUser.getRole()) && !"admin".equalsIgnoreCase(loggedInUser.getRole()))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        // Connection conn = null; // No longer needed here as DAOs manage their own connections
        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("DEBUG: GetStudentsServlet received request body: " + requestBody);

            JsonObject requestData;
            try {
                requestData = gson.fromJson(requestBody, JsonObject.class);
                if (requestData == null) {
                    throw new JsonSyntaxException("Request body is empty or malformed JSON.");
                }
            } catch (JsonSyntaxException e) {
                System.err.println("JSON parsing error in GetStudentsServlet: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid JSON format in request: " + e.getMessage());
                out.print(gson.toJson(jsonResponse));
                return;
            }

            int programId = -1; // Default to an invalid ID
            int semester = -1;
            
            // Check if 'programId' is present and not null in the request JSON
            if (requestData.has("programId") && !requestData.get("programId").isJsonNull()) {
                try {
                    programId = requestData.get("programId").getAsInt();
                    // CORRECTED: Use "semester" key as sent from frontend
                    if (requestData.has("semester") && !requestData.get("semester").isJsonNull()) {
                        semester = requestData.get("semester").getAsInt();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid programId or semester format received. ProgramId: " + requestData.get("programId") + ", Semester: " + requestData.get("semester"));
                    // Continue with default -1, or set error status if these are mandatory
                }
            }

            List<Student> students;
            // Decide which method to call based on programId
            if (programId != -1 && semester != -1) { // Both programId and semester are provided
                System.out.println("DEBUG: GetStudentsServlet: Fetching students for programId: " + programId + " and semester: " + semester);
                students = attendanceDAO.getStudentsByProgramAndSemester(programId, semester);
            } else {
                // If no programId/semester is provided (or it's invalid), get all students (e.g., for Manage Students)
                System.out.println("DEBUG: GetStudentsServlet: Fetching ALL students.");
                students = userDAO.getAllStudents(); // Call the getAllStudents from UserDAO
            }

            // Serialize the list of Student objects into the JSON response
            jsonResponse.addProperty("status", "success");
            jsonResponse.add("students", gson.toJsonTree(students)); // Wrap list in a JSON object
            out.print(gson.toJson(jsonResponse));

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQLException in GetStudentsServlet: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error fetching students: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } catch (Exception e) { // Catch broader exceptions for request parsing or other issues
            e.printStackTrace();
            System.err.println("Unexpected error in GetStudentsServlet: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Use BAD_REQUEST for parsing errors
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing request: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } finally {
            // No need to close connection here, DBUtil manages the pool
            out.flush(); // Ensure all buffered output is sent
        }
    }
}
