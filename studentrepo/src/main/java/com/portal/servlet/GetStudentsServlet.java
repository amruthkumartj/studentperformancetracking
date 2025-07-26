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

 // In com/portal/servlet/GetStudentsServlet.java

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        HttpSession session = request.getSession(false);

        // Authentication Check
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            out.print(gson.toJson(jsonResponse));
            return;
        }

        try {
            String requestBody = request.getReader().lines().collect(java.util.stream.Collectors.joining(System.lineSeparator()));
            JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

            String searchTerm = null;
            if (requestData != null && requestData.has("searchTerm")) {
                searchTerm = requestData.get("searchTerm").getAsString();
            }

            UserDAO userDAO = new UserDAO();
            List<Student> students;

            // If a search term is provided, filter students by name
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                students = userDAO.searchStudentsByName(searchTerm); // We will create this new DAO method
            } else {
                // Otherwise, get all students as before
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