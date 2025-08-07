package com.portal.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.portal.datatransfer_access.UserDAO;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L; // Recommended for servlets

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- 1. Get All Form Parameters ---
        String userIdField = request.getParameter("regUsername"); // This is the USERID for students, or USERNAME for faculty
        String password = request.getParameter("regPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role");
        String email = request.getParameter("regEmail"); // NEW: Get the email parameter

        // --- 2. Basic Validation (now includes email) ---
        if (userIdField == null || password == null || confirmPassword == null || role == null || email == null ||
                userIdField.trim().isEmpty() || password.trim().isEmpty() ||
                confirmPassword.trim().isEmpty() || role.trim().isEmpty() || email.trim().isEmpty()) {
            System.out.println("❌ Registration Blocked: Missing required fields (including email).");
            response.sendRedirect("login.html?reg=fail&reason=missingfields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("❌ Registration Blocked: Passwords do not match.");
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
            String studentName = dao.getStudentNameById(studId); // Ensure this method exists and works
            System.out.println("[DEBUG] Fetched name from students table: '" + studentName + "'");

            // Validate that a name was actually found for the given ID
            if (studentName == null || studentName.trim().isEmpty()) {
                System.out.println("❌ Registration Blocked: Student ID " + studId + " not found in students table or has no name.");
                response.sendRedirect("login.html?reg=fail&reason=studentnotfound");
                return;
            }

            // Assign the fetched name to be the username in the 'users' table.
            usernameForRegistration = studentName.trim();

            // Check if a user account with this username has already been registered.
           

        } else if (role.equalsIgnoreCase("faculty")) {
            System.out.println("[DEBUG] Role selected: Faculty");

            usernameForRegistration = userIdField.trim(); // For faculty, userIdField is the desired username

            if (usernameForRegistration.matches("\\d+")) { // Check if the string consists ONLY of digits
                System.out.println("❌ Registration Blocked: Faculty username cannot be purely numeric. Please provide a name.");
                response.sendRedirect("login.html?reg=fail&reason=invalidfacultyusername");
                return;
            }

           
        } else if (role.equalsIgnoreCase("admin")) { // Added Admin role handling if allowed to self-register
             System.out.println("[DEBUG] Role selected: Admin");
             usernameForRegistration = userIdField.trim();

            
        }
        else {
            System.out.println("❌ Invalid role provided: " + role);
            response.sendRedirect("login.html?reg=fail&reason=invalidrole");
            return;
        }

        System.out.println("[DEBUG] Attempting to register user with FINAL username: '" + usernameForRegistration +
                           "', email: '" + email + "' and role: '" + role.toUpperCase() + "'");

        try {
            // Pass the email to the DAO's register method
            int generatedUserId = dao.register(usernameForRegistration, password, role.toUpperCase(), email);

            if (generatedUserId > 0) {
                System.out.println("✅ Registration successful for: " + usernameForRegistration);
                response.sendRedirect("login.html?reg=success");
            } else if (generatedUserId == -2) { // DAO returned -2 for email already taken
                System.out.println("❌ Registration failed: Email '" + email + "' is already registered.");
                response.sendRedirect("login.html?reg=fail&reason=emailtaken");
            } else if (generatedUserId == -3) { // DAO returned -3 for username already taken (pre-check or constraint)
                System.out.println("❌ Registration failed: Username '" + usernameForRegistration + "' is already taken.");
                response.sendRedirect("login.html?reg=fail&reason=usernametaken");
            } else { // General database error or other unhandled failure
                System.out.println("❌ Registration failed in DAO for: " + usernameForRegistration + " (General DB error).");
                response.sendRedirect("login.html?reg=fail&reason=dberror");
            }
        } catch (IllegalArgumentException e) {
            // Catches the exception thrown by UserDAO.register for invalid email suffix
            System.out.println("❌ Registration Blocked: Invalid email domain for '" + email + "'. Error: " + e.getMessage());
            response.sendRedirect("login.html?reg=fail&reason=invalidemaildomain");
        } catch (Exception e) {
            System.err.println("❌ An unexpected error occurred during registration: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("login.html?reg=fail&reason=error");
        }
    }
}