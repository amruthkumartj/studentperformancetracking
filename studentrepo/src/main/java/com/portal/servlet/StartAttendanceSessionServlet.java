package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.portal.AttendanceDAO;
import com.portal.AttendanceSession;
import com.portal.DBUtil;
import com.portal.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
// import java.time.LocalDateTime; // REMOVE THIS IMPORT as you're no longer using LocalDateTime for session times
import java.time.Instant; // ADD THIS IMPORT
import java.time.Duration; // ADD THIS IMPORT for easily adding time
import java.time.Year;

@WebServlet("/StartAttendanceSessionServlet")
public class StartAttendanceSessionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

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

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            AttendanceDAO attendanceDAO = new AttendanceDAO(conn);

            JsonObject requestData = gson.fromJson(request.getReader(), JsonObject.class);
            
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
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing request: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        out.print(gson.toJson(jsonResponse));
    }
}