package com.portal.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.portal.User;    // Ensure your User DTO is correctly imported
import com.portal.datatransfer_access.UserDAO;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // ⚠️ START OF MODIFIED LOGIC FOR SESSION VALIDATION ⚠️
        String action = req.getParameter("action");
        if ("validate".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                // If session is invalid, immediately redirect to logout page
                resp.sendRedirect(req.getContextPath() + "/logout");
                return; // Stop further execution
            } else {
                // Session is valid, do nothing and let the request complete.
                // This is a simple validation endpoint.
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().println("Session valid");
                return;
            }
        }
        // ⚠️ END OF MODIFIED LOGIC ⚠️

        /* Original logic: Just bounce to the login page */
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // The 'username' field from login.html will now serve as the general 'identifier'
        String identifier = req.getParameter("username");
        String password = req.getParameter("password");
        String requestedRole = req.getParameter("role"); // This comes from the dropdown/segmented button

        /* 1 ── Missing fields ─────────────────────────────── */
        if (isEmpty(identifier) || isEmpty(password) || isEmpty(requestedRole)) {
            System.out.println("❌ LoginServlet: Missing fields - identifier, password, or role is empty.");
            resp.sendRedirect(req.getContextPath()
                    + "/login.html?login=fail&reason=missingfields");
            return;
        }

        /* 2 ── Validate user using the updated UserDAO.validate method ── */
        UserDAO dao = new UserDAO();
        User user = null;
        String reason = "invalid"; // Default reason for failed login

        try {
            // Call the updated validate method in UserDAO
            user = dao.validate(identifier.trim(), password.trim(), requestedRole.trim());

            if (user != null) {
            	System.out.println("--- LOGIN SUCCESSFUL: DEBUGGING USER SESSION DATA ---");
                System.out.println("User ID from User object: " + user.getId());
                System.out.println("Username from User object: " + user.getUsername());
                System.out.println("Email from User object: " + user.getEmail());
                System.out.println("Role from User object: " + user.getRole());
                System.out.println("----------------------------------------------------");
                /* 3 ── Success: create session + redirect to dashboard */
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user); // Store the full User object
                // You might also store specific attributes for easier access in JSPs
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("isApproved", user.isApproved());


                String ctx = req.getContextPath();
                String redirectUrl = "";

                // Use user.getRole() for redirection, as this is the actual role from DB
                if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                    redirectUrl = ctx + "/studentdashboard";
                } else if ("FACULTY".equalsIgnoreCase(user.getRole())) {
                    redirectUrl = ctx + "/facultydashboard";
                } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    // As per your requirement, ADMIN goes to facultydashboard
                    redirectUrl = ctx + "/facultydashboard";
                } else {
                    // Fallback for unexpected roles (shouldn't happen if roles are controlled)
                    System.err.println("LoginServlet: Unexpected user role after successful login: " + user.getRole());
                    redirectUrl = ctx + "/login.html?login=fail&reason=invalidrole";
                }
                
                long timestamp = System.currentTimeMillis();
                redirectUrl += "?t=" + timestamp;

                System.out.println("DEBUG: Login successful for " + user.getUsername() + ", redirecting to: " + redirectUrl);
                resp.sendRedirect(redirectUrl);

            } else {
                /* 4 ── Login failed. Determine specific reason if possible, otherwise default to 'invalid'. */
                // The UserDAO.validate method now returns null for all failure cases (invalid credentials,
                // role mismatch, unapproved faculty). We can't distinguish all specific reasons here
                // without more complex return values from DAO or re-checking.
                // For unapproved faculty, the DAO already returns null. Let's provide a more specific message if possible.

                // To distinguish 'pendingapproval' for faculty, we need to re-check if the identifier belongs to an unapproved faculty.
                // This is a bit redundant with DAO, but necessary for specific frontend message.
                try {
                    // If the requested role was faculty and identifier was an email, check if that email belongs to an unapproved faculty
                    if ("FACULTY".equalsIgnoreCase(requestedRole) && identifier.contains("@")) { // Simple check for email format
                        User potentialUser = dao.getUserByEmail(identifier.trim()); // Assuming you add this method to UserDAO
                        if (potentialUser != null && "FACULTY".equalsIgnoreCase(potentialUser.getRole()) && !potentialUser.isApproved()) {
                            reason = "pendingapproval";
                        }
                    }
                    // If the requested role was faculty and identifier was numeric (faculty ID), check if that ID belongs to an unapproved faculty
                    else if ("FACULTY".equalsIgnoreCase(requestedRole) && identifier.matches("\\d+")) {
                        // This requires a new DAO method to get user details by faculty ID
                        User potentialUser = dao.getUserByFacultyId(Integer.parseInt(identifier.trim())); // Assuming this method exists and returns a User
                        if (potentialUser != null && "FACULTY".equalsIgnoreCase(potentialUser.getRole()) && !potentialUser.isApproved()) {
                            reason = "pendingapproval";
                        }
                    }
                } catch (Exception e) {
                    System.err.println("LoginServlet: Error trying to determine specific login failure reason: " + e.getMessage());
                    // Fallback to default 'invalid' reason
                }

                System.out.println("DEBUG: Login failed for identifier " + identifier + ", reason: " + reason);
                resp.sendRedirect(req.getContextPath() + "/login.html?login=fail&reason=" + reason);
            }
        } catch (Exception e) { // Catch any unexpected exceptions from DAO or other logic
            System.err.println("LoginServlet: Critical error during login processing: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/login.html?login=fail&reason=error");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}