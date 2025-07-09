package com.portal.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.portal.*;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /* just bounce to the login page (GET is not allowed) */
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username      = req.getParameter("username");
        String password      = req.getParameter("password");
        String requestedRole = req.getParameter("role");   // hidden input

        /* 1 ── Missing fields ─────────────────────────────── */
        if (isEmpty(username) || isEmpty(password) || isEmpty(requestedRole)) {
            resp.sendRedirect(req.getContextPath()
                    + "/login.html?login=fail&reason=missingfields");
            return;
        }

        /* 2 ── Validate user ──────────────────────────────── */
        UserDAO dao = new UserDAO();
        User user   = dao.validate(username.trim(),
                                   password.trim(),
                                   requestedRole.trim());

        if (user != null &&
            requestedRole.equalsIgnoreCase(user.getRole())) {

            /* 3 ── success: create session + redirect to dashboard */
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);

            String ctx = req.getContextPath();
            if ("student".equalsIgnoreCase(user.getRole())) {
                resp.sendRedirect(ctx + "/student/dashboard");
            } else {
                resp.sendRedirect(ctx + "/facultydashboard");
            }

        } else {
            /* 4 ── invalid credentials / role mismatch */
            resp.sendRedirect(req.getContextPath()
                    + "/login.html?login=fail&reason=invalid");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
