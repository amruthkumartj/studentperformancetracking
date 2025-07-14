package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.portal.AttendanceDAO; // Still needed for getStudentsByProgram
import com.portal.DBUtil;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/GetStudentsServlet")
public class GetStudentsServlet extends HttpServlet {
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

        // Authentication and Authorization check
        if (loggedInUser == null || (!"faculty".equalsIgnoreCase(loggedInUser.getRole()) && !"admin".equalsIgnoreCase(loggedInUser.getRole()))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        Connection conn = null;
        try {
            JsonObject requestData = gson.fromJson(request.getReader(), JsonObject.class);

            int programId = -1; // Default to an invalid ID
            int semester = -1;
            // Check if 'programId' is present and not null in the request JSON
            if (requestData != null && requestData.has("programId") && !requestData.get("programId").isJsonNull()) {
                try {
                    programId = requestData.get("programId").getAsInt();
                    // CORRECTED: Use "semester" key as sent from frontend
                    if (requestData.has("semester") && !requestData.get("semester").isJsonNull()) {
                        semester = requestData.get("semester").getAsInt(); 
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid programId or semester format received. ProgramId: " + requestData.get("programId") + ", Semester: " + requestData.get("semester"));
                }
            }

            conn = DBUtil.getConnection();
            // Instantiate both DAOs as needed
            AttendanceDAO attendanceDAO = new AttendanceDAO(conn); // For getStudentsByProgram
            UserDAO userDAO = new UserDAO(); // UserDAO doesn't need a connection passed in constructor from its code

            List<Student> students;
            // Decide which method to call based on programId
            if (programId != -1) {
                // If programId is provided and valid, filter by program (e.g., for attendance)
                System.out.println("DEBUG: GetStudentsServlet: Fetching students for programId: " + programId);
                students = attendanceDAO.getStudentsByProgramAndSemester(programId,semester);
            } else {
                // If no programId is provided (or it's invalid), get all students (e.g., for Manage Students)
                System.out.println("DEBUG: GetStudentsServlet: Fetching ALL students.");
                students = userDAO.getAllStudents(); // Call the getAllStudents from UserDAO
            }

            // Serialize the list of Student objects directly to JSON
            out.print(gson.toJson(students));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error fetching students: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } catch (Exception e) { // Catch broader exceptions for request parsing or other issues
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing request: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}