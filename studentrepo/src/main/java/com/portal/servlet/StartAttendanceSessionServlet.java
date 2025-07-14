package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.portal.AttendanceDAO;
import com.portal.AttendanceSession;
import com.portal.DBUtil;
import com.portal.User; // Import User for faculty_id

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
import java.time.LocalDateTime;
import java.time.Year; // For academic year

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
            // NOTE: programId and semester are no longer sent directly from faculty.js for StartAttendanceSessionServlet
            // Instead, courseId and topic are sent. We need to get programId and semester from the course.
            String courseIdFromUI = requestData.get("courseId").getAsString(); // This is like "MCA101"
            String topicFromUI = requestData.get("topic").getAsString(); // This is like "Digital Logic Design"
            int facultyId = requestData.get("facultyId").getAsInt();
            // String clientDateTime = requestData.get("clientDateTime").getAsString(); // If you need to log client time

            // Fetch course details to get programId and semester
            // This requires a new DAO method or modifying an existing one to get Course object by courseId
            // For now, let's assume we can get it from AttendanceDAO.getAttendanceSession if we pass a dummy session ID
            // OR, better, create a new DAO method: getCourseDetailsByCourseId(String courseId)
            
            // For simplicity, let's just use the courseId directly and rely on the DAO to fetch full session details later
            // The programId and semester will be populated when getAttendanceSession is called later.

            // Create a new AttendanceSession object
            AttendanceSession newSession = new AttendanceSession();
            newSession.setCourseId(courseIdFromUI); // Set the actual course_id (e.g., "BCA102")
            newSession.setTopic(topicFromUI); // Use the course_name as topic
            newSession.setLocation("Classroom A"); // Placeholder, can be dynamic
            newSession.setFacultyId(facultyId); // Use the facultyId from the request
            newSession.setSessionStartTime(LocalDateTime.now());
            newSession.setSessionExpiryTime(LocalDateTime.now().plusMinutes(15)); // 15-minute session
            newSession.setStatus("ACTIVE");

            int generatedSessionId = attendanceDAO.createAttendanceSession(newSession); // This sets newSession.sessionId

            if (generatedSessionId != -1) {
                // Retrieve the full session object with programName, subjectName, semester, programId populated
                AttendanceSession fullSession = attendanceDAO.getAttendanceSession(generatedSessionId);
                
                if (fullSession != null) {
                    session.setAttribute("currentAttendanceSession", fullSession); // Store full session in HTTP session
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Attendance session started successfully!");
                    jsonResponse.addProperty("sessionId", fullSession.getSessionId()); // Send the int session ID
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
