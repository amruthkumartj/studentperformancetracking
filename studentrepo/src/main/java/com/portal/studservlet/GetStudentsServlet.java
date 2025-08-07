// src/main/java/com/portal/servlet/GetStudentsServlet.java
package com.portal.studservlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException; // Import for JsonSyntaxException
import com.portal.DBUtil; // Keep this import as DAOs use it
import com.portal.Student;
import com.portal.User;
import com.portal.datatransfer_access.AttendanceDAO;
import com.portal.datatransfer_access.UserDAO;

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

 // In com/portal/servlet/GetStudentsServlet.java

 // In: GetStudentsServlet.java
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        // It's good practice to ensure the user is authenticated
        if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            out.print(gson.toJson(jsonResponse));
            return;
        }

        try {
            String requestBody = request.getReader().lines().collect(java.util.stream.Collectors.joining(System.lineSeparator()));
            JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

            List<Student> students;

            // ✅ CORRECTED LOGIC
            // First, check if the request is for taking attendance (has programId and semester)
            if (requestData != null && requestData.has("programId") && requestData.has("semester")) {
                int programId = requestData.get("programId").getAsInt();
                int semester = requestData.get("semester").getAsInt();
                
                // Call a new DAO method to get filtered students
                students = userDAO.getStudentsByProgramAndSemester(programId, semester);

            } 
            // Second, check if the request is for the search bar (has searchTerm)
            else if (requestData != null && requestData.has("searchTerm")) {
                String searchTerm = requestData.get("searchTerm").getAsString();
                students = userDAO.searchStudentsByName(searchTerm);
                
            } 
            // Fallback to getting all students if the request is empty
            else {
                students = userDAO.getAllStudents();
            }

            // Return the list of students as JSON
            out.print(gson.toJson(students));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error fetching students: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }}