package com.portal.studservlet; // Replace with your actual package name

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.portal.Student; // Assuming your Student model is here
import com.portal.datatransfer_access.UserDAO;

@WebServlet("/DeleteStudentServlet")
public class DeleteStudentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO UserDAO; // Instantiate your DAO

    public void init() {
        // Initialize your StudentDAO here, possibly getting connection pool etc.
        // Example: studentDAO = new StudentDAO();
        // Or get from ServletContext if you've set it up there
        UserDAO = new UserDAO(); // Adjust according to how you manage DAO instances
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String studentId = request.getParameter("studentId"); // Get studentId from request parameter

        if (studentId == null || studentId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\": \"error\", \"message\": \"Student ID is required.\"}");
            out.flush();
            return;
        }

        try {
            boolean deleted = UserDAO.deleteStudent(studentId); // Call your DAO method

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\": \"success\", \"message\": \"Student deleted successfully.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Or SC_INTERNAL_SERVER_ERROR if DB failed
                out.print("{\"status\": \"error\", \"message\": \"Student with ID " + studentId + " not found or could not be deleted.\"}");
            }
        } catch (Exception e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\": \"error\", \"message\": \"An error occurred during deletion: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}