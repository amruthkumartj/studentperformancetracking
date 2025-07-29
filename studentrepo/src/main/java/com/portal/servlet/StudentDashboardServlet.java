package com.portal.servlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.portal.User;
import com.portal.UserDAO;
import com.portal.StudentDashboardDTO;

@WebServlet("/studentdashboard")
public class StudentDashboardServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Authentication Check
        User u = (User) req.getSession().getAttribute("user");
        if (u == null || !"STUDENT".equals(u.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        try {
            // 2. Fetch all dashboard data using the new, optimized DAO method
            StudentDashboardDTO dashboardDTO = userDAO.getStudentDashboardInfo(u.getId());
            
            // Check if data was found for the student
            if (dashboardDTO.getStudentName() == null) {
                 // This can happen if a user with role STUDENT exists but has no entry in the students table
                System.err.println("No student data found for user_id: " + u.getId());
                // You might want to redirect to an error page or show a message
                resp.sendRedirect(req.getContextPath() + "/login.html?error=nodata");
                return;
            }

            // 3. Set the single DTO object as a request attribute
            req.setAttribute("dashboard", dashboardDTO);

            // 4. Forward to the JSP page
            req.getRequestDispatcher("/studentDashboard.jsp").forward(req, resp);

        } catch (SQLException ex) {
            System.err.println("Database error in StudentDashboardServlet: " + ex.getMessage());
            ex.printStackTrace();
            // In a real application, you'd forward to a user-friendly error page
            throw new ServletException("Error retrieving student data.", ex);
        }
    }
}