package com.portal.studservlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson; // Import Gson for JSON responses
import com.google.gson.JsonObject; // Import JsonObject for structured JSON error responses
import com.portal.datatransfer_access.UserDAO;

@WebServlet("/AddStudServlet")
public class AddStudServlet extends HttpServlet {

    private Gson gson = new Gson(); // Initialize Gson for JSON serialization

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8"); // Set content type to JSON
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject(); // Create a JSON object for the response

        try {
            // Parse numerical parameters first
            int studentId = Integer.parseInt(req.getParameter("studentId"));
            int semester = Integer.parseInt(req.getParameter("semester"));
            int programId = Integer.parseInt(req.getParameter("programId"));

            String fullName = req.getParameter("fullName");
            String phone = req.getParameter("phone");
            String email = req.getParameter("email");

            UserDAO dao = new UserDAO(); // Instantiate UserDAO

            // Input validation checks (re-using existing logic)
            if (dao.studentIdExists(studentId)) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Student ID already exists.");
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                out.print(gson.toJson(jsonResponse));
                return;
            }
            if (dao.emailExists(email)) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Email already registered.");
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                out.print(gson.toJson(jsonResponse));
                return;
            }
            if (dao.phoneExists(phone)) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Phone number already registered.");
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            // Call the updated addStudent method in UserDAO
            // This method now handles inserting into 'students' and 'student_courses'
            boolean success = dao.addStudent(studentId, fullName, programId, semester, phone, email);

            if (success) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Student added and enrolled in courses successfully!");
                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to add student and/or enroll in courses due to a database error. Please check server logs.");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            }
            out.print(gson.toJson(jsonResponse));

        } catch (NumberFormatException ex) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Student ID, Program ID, and Semester must be valid numbers.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            out.print(gson.toJson(jsonResponse));
            System.err.println("NumberFormatException in AddStudServlet: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) { // Catch any other unexpected exceptions (e.g., SQLException from DAO)
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "An unexpected server error occurred during student addition/enrollment: " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            out.print(gson.toJson(jsonResponse));
            System.err.println("General Exception in AddStudServlet: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            out.flush();
        }
    }
}
