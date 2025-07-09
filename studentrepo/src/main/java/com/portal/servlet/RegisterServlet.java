package com.portal.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.portal.UserDAO;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- 1. Get All Form Parameters ---
        String userIdField = request.getParameter("regUsername"); // This is the USERID from the form
        String password = request.getParameter("regPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role");

        // --- 2. Basic Validation ---
        if (userIdField == null || password == null || confirmPassword == null || role == null ||
                userIdField.trim().isEmpty() || password.trim().isEmpty() ||
                confirmPassword.trim().isEmpty() || role.trim().isEmpty()) {
            response.sendRedirect("login.html?reg=fail&reason=missingfields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            response.sendRedirect("login.html?reg=fail&reason=nomatch");
            return;
        }

        UserDAO dao = new UserDAO();
        String usernameForRegistration; // This will hold the final username for the 'users' table.

        // --- 3. Role-Specific Logic ---
        if (role.equalsIgnoreCase("student")) {
            System.out.println("[DEBUG] Role selected: Student");
            int studId;
            try {
                studId = Integer.parseInt(userIdField.trim());
                System.out.println("[DEBUG] Parsed student ID from form: " + studId);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid Student ID format: " + userIdField);
                response.sendRedirect("login.html?reg=fail&reason=invalidid");
                return;
            }

            // --- CORE FIX: Fetch the student's name using their ID ---
            String studentName = dao.getStudentNameById(studId);
            System.out.println("[DEBUG] Fetched name from students table: '" + studentName + "'");

            // Validate that a name was actually found for the given ID
            if (studentName == null || studentName.trim().isEmpty()) {
                System.out.println("❌ Registration Blocked: Student ID " + studId + " not found in students table or has no name.");
                response.sendRedirect("login.html?reg=fail&reason=studentnotfound");
                return;
            }
            
            // Assign the fetched name to be the username in the 'users' table.
            usernameForRegistration = studentName.trim();

            // Check if a user account with this name has already been registered.
            if (dao.isUsernameTaken(usernameForRegistration)) {
                System.out.println("❌ Registration Blocked: An account for user '" + usernameForRegistration + "' already exists.");
                response.sendRedirect("login.html?reg=fail&reason=alreadyregistered");
                return;
            }

        } else if (role.equalsIgnoreCase("faculty")) {
            System.out.println("[DEBUG] Role selected: Faculty");
           
            usernameForRegistration = userIdField.trim();

            
            if (usernameForRegistration.matches(".*\\d.*")) { // Check if the string consists ONLY of digits
                System.out.println("❌ Registration Blocked: Faculty username cannot be purely numeric. Please provide a name.");
                response.sendRedirect("login.html?reg=fail&reason=invalidfacultyusername"); // A new, specific reason
                return;
            }
    
            if (dao.isUsernameTaken(usernameForRegistration)) {
                System.out.println("❌ Registration Blocked: Username '" + usernameForRegistration + "' is already taken.");
                response.sendRedirect("login.html?reg=fail&reason=usernametaken");
                return;
            }
        } else {
            
            System.out.println("❌ Invalid role provided: " + role);
            response.sendRedirect("login.html?reg=fail&reason=invalidrole");
            return;
        }

      
        System.out.println("[DEBUG] Attempting to register user with FINAL username: '" + usernameForRegistration + "' and role: '" + role.toUpperCase() + "'");
        int generatedUserId = dao.register(usernameForRegistration, password, role.toUpperCase());

        if (generatedUserId > 0) {
            System.out.println("✅ Registration successful for: " + usernameForRegistration);
            response.sendRedirect("login.html?reg=success");
        } else {
            System.out.println("❌ Registration failed in DAO for: " + usernameForRegistration);
            response.sendRedirect("login.html?reg=fail&reason=dberror");
        }
    }
}