package com.portal.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.portal.*; // Assuming User and UserDAO are in com.portal package

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /* just bounce to the login page (GET is not allowed for direct login processing) */
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username      = req.getParameter("username");
        String password      = req.getParameter("password");
        String requestedRole = req.getParameter("role"); // hidden input from frontend

        /* 1 ── Missing fields ─────────────────────────────── */
        if (isEmpty(username) || isEmpty(password) || isEmpty(requestedRole)) {
            resp.sendRedirect(req.getContextPath()
                    + "/login.html?login=fail&reason=missingfields");
            return;
        }

        /* 2 ── Validate user ──────────────────────────────── */
        UserDAO dao = new UserDAO();
        User user   = null;
        try {
            user = dao.validate(username.trim(),
                                password.trim(),
                                requestedRole.trim());
        } catch (Exception e) { // Catch any exceptions from DAO validation
            System.err.println("Error during user validation in LoginServlet: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/login.html?login=fail&reason=error");
            return;
        }


        // --- CRITICAL FIX HERE ---
        // The user is successfully found and validated by UserDAO.validate.
        // Now, check if the user is an ADMIN, OR if the requested role matches the actual user role.
        // UserDAO.validate already handles password check, role match, and faculty approval status.
        if (user != null) { // If user is not null, it means validate passed all checks
            /* 3 ── success: create session + redirect to dashboard */
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);

            String ctx = req.getContextPath();
            String redirectUrl = "";

            // Use user.getRole() for redirection, as this is the actual role from DB
            if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                redirectUrl = ctx + "/studentdashboard"; // Corrected: Removed '/student/'
            } else if ("FACULTY".equalsIgnoreCase(user.getRole())) {
                redirectUrl = ctx + "/facultydashboard";
            } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                // As per your requirement, ADMIN goes to facultydashboard
                redirectUrl = ctx + "/facultydashboard";
            } else {
                // Fallback for unexpected roles (shouldn't happen with current roles)
                System.err.println("Unexpected user role after successful login: " + user.getRole());
                redirectUrl = ctx + "/login.html?login=fail&reason=invalidrole";
            }
            System.out.println("DEBUG: Login successful for " + user.getUsername() + ", redirecting to: " + redirectUrl);
            resp.sendRedirect(redirectUrl);

        } else {
            /* 4 ── Login failed. Determine the specific reason for redirection. */
            String reason = "invalid"; // Default reason for invalid credentials/role mismatch

            // To provide a more specific message for unapproved faculty,
            // we check if the username exists as a faculty, and if validate returned null.
            // This heuristic is still useful for user-facing messages.
            try {
                // Fetch the user by username to check their status/role more precisely for error message
                User potentialUser = dao.getUserById(dao.getUserIdByUsername(username.trim())); // Assuming getUserIdByUsername exists
                if (potentialUser != null && "FACULTY".equalsIgnoreCase(potentialUser.getRole()) && !potentialUser.isApproved()) {
                    reason = "pendingapproval";
                }
            } catch (SQLException e) {
                System.err.println("Error checking user status for login failure message: " + e.getMessage());
                // Fallback to default reason if DB error
            }


            System.out.println("DEBUG: Login failed for " + username + ", reason: " + reason);
            resp.sendRedirect(req.getContextPath() + "/login.html?login=fail&reason=" + reason);
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}