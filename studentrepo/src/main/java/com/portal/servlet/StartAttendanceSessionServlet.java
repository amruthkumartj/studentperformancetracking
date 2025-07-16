// src/main/java/com/portal/servlet/StartAttendanceSessionServlet.java
package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException; // Import for JsonSyntaxException
import com.portal.AttendanceDAO;
import com.portal.AttendanceSession;
import com.portal.DBUtil; // Keep this import as DAOs use it
import com.portal.User;

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
import java.time.Instant;
import java.time.Duration;
import java.time.Year; // This import might not be strictly needed unless used elsewhere in the servlet

@WebServlet("/StartAttendanceSessionServlet")
public class StartAttendanceSessionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();
    private AttendanceDAO attendanceDAO; // Declare DAO instance

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize AttendanceDAO here, it no longer needs a Connection in its constructor
        attendanceDAO = new AttendanceDAO();
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

        if (loggedInUser == null || (!"faculty".equalsIgnoreCase(loggedInUser.getRole()) && !"admin".equalsIgnoreCase(loggedInUser.getRole()))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        // Connection conn = null; // No longer needed here as DAO manages its own connections
        try {
            String requestBody = request.getReader().lines().collect(java.util.stream.Collectors.joining(System.lineSeparator()));
            System.out.println("DEBUG: StartAttendanceSessionServlet received request body: " + requestBody);

            JsonObject requestData;
            try {
                requestData = gson.fromJson(requestBody, JsonObject.class);
                if (requestData == null) {
                    throw new JsonSyntaxException("Request body is empty or malformed JSON.");
                }
            } catch (JsonSyntaxException e) {
                System.err.println("JSON parsing error in StartAttendanceSessionServlet: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid JSON format in request: " + e.getMessage());
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            String courseIdFromUI = requestData.get("courseId").getAsString();
            String topicFromUI = requestData.get("topic").getAsString();
            int facultyId = requestData.get("facultyId").getAsInt();
            
            // Placeholder for location - make dynamic if needed
            String location = "Classroom A"; 

            // --- CRITICAL CHANGE HERE: Use Instant.now() and Duration ---
            Instant sessionStartTime = Instant.now(); // This is the current time in UTC
            // Set expiry for 15 minutes from now, in UTC
            Instant sessionExpiryTime = sessionStartTime.plus(Duration.ofMinutes(15)); 
            // --- END CRITICAL CHANGE ---

            // Create a new AttendanceSession object
            AttendanceSession newSession = new AttendanceSession();
            newSession.setCourseId(courseIdFromUI);
            newSession.setTopic(topicFromUI);
            newSession.setLocation(location);
            newSession.setFacultyId(facultyId);
            newSession.setSessionStartTime(sessionStartTime); // Now passing Instant
            newSession.setSessionExpiryTime(sessionExpiryTime); // Now passing Instant
            newSession.setStatus("ACTIVE");

            int generatedSessionId = attendanceDAO.createAttendanceSession(newSession); // This sets newSession.sessionId

            if (generatedSessionId != -1) {
                // Retrieve the full session object with programName, subjectName, semester, programId populated
                AttendanceSession fullSession = attendanceDAO.getAttendanceSession(generatedSessionId);
                
                if (fullSession != null) {
                    session.setAttribute("currentAttendanceSession", fullSession); // Store full session in HTTP session
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Attendance session started successfully!");
                    jsonResponse.addProperty("sessionId", fullSession.getSessionId());
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to retrieve full session details after creation.");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to start attendance session (DAO error).");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Set status for DB errors
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing request: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Set status for general processing errors
        } finally {
            // No need to close connection here, DBUtil manages the pool
            out.flush(); // Ensure all buffered output is sent
        }
        out.print(gson.toJson(jsonResponse));
    }
}
