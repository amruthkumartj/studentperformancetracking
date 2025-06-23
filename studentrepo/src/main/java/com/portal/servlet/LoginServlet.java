package com.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<h3>Login must be done with POST.</h3>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String requestedRole = req.getParameter("role"); // From login.html dropdown

        try (PrintWriter out = resp.getWriter()) {

            if (username == null || password == null || requestedRole == null) {
                out.println("<script type=\"text/javascript\">");
                out.println("alert('Missing login fields.');");
                out.println("location='" + req.getContextPath() + "/login.html';");
                out.println("</script>");
                return;
            }

            UserDAO dao = new UserDAO();
            User user = dao.validate(username.trim(), password.trim(), requestedRole.trim());

            if (user != null && requestedRole.equalsIgnoreCase(user.getRole())) {

                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);

                String ctx = req.getContextPath();
                if ("student".equalsIgnoreCase(user.getRole())) {
                    resp.sendRedirect(ctx + "/student/dashboard");
                } else {
                    resp.sendRedirect(ctx + "/facultydashboard");
                }

            } else {
                out.println("<script type=\"text/javascript\">");
                out.println("alert('Invalid username, password, or role selection.');");
                out.println("location='" + req.getContextPath() + "/login.html';");
                out.println("</script>");
            }
        }
    }
}
