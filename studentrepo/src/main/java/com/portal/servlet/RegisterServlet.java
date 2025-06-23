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

        String username = request.getParameter("regUsername");
        String password = request.getParameter("regPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role");

        // Validate null or empty inputs
        if (username == null || password == null || confirmPassword == null || role == null ||
                username.trim().isEmpty() || password.trim().isEmpty() ||
                confirmPassword.trim().isEmpty() || role.trim().isEmpty()) {
            System.out.println("❌ Missing or empty registration fields.");
            response.sendRedirect("login.html?reg=fail&reason=missingfields");
            return;
        }

        username = username.trim();
        role = role.trim();

        System.out.println("🔧 RegisterServlet called with username: " + username + ", role: " + role);

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            System.out.println("❌ Passwords do not match.");
            response.sendRedirect("login.html?reg=fail&reason=nomatch");
            return;
        }

        // Validate role
        if (!role.equalsIgnoreCase("faculty") && !role.equalsIgnoreCase("student")) {
            System.out.println("❌ Invalid role provided: " + role);
            response.sendRedirect("login.html?reg=fail&reason=invalidrole");
            return;
        }

        // Perform registration and get generated user_id
        UserDAO dao = new UserDAO();
        // ✅ Uppercase role here (so DB always stores as "STUDENT" or "FACULTY")
        int userId = dao.register(username, password, role.toUpperCase());

        if (userId > 0) {
            System.out.println("✅ Registration success for: " + username + " | user_id: " + userId);
            response.sendRedirect("login.html?reg=success");
        } else {
            System.out.println("❌ Registration failed for: " + username);
            response.sendRedirect("login.html?reg=fail");
        }
    }
}
