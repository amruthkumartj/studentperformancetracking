package com.portal.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.portal.User;

@WebServlet("/facultydashboard")
public class FacultyDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User u = (User) req.getSession().getAttribute("user");
        if (u == null || !"FACULTY".equals(u.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }
        req.getRequestDispatcher("/facultyDashboard.jsp").forward(req, resp);
    }
}
