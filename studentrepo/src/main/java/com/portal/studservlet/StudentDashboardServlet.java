package com.portal.studservlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.portal.User;
import com.portal.datatransfer_access.StudentDashboardDTO;
import com.portal.datatransfer_access.UserDAO;

@WebServlet("/studentdashboard")
public class StudentDashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(StudentDashboardServlet.class.getName());

    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User u = (User) req.getSession().getAttribute("user");
        
        LOGGER.info("StudentDashboardServlet accessed. User ID from session: " + (u != null ? u.getId() : "null"));

        if (u == null || !"STUDENT".equals(u.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        try {
            StudentDashboardDTO dashboardDTO = userDAO.getStudentDashboardInfo(u.getId());

            if (dashboardDTO == null) {
                System.err.println("No student data found for user_id: " + u.getId());
                resp.sendRedirect(req.getContextPath() + "/login.html?error=nodata");
                return;
            }
            
            LOGGER.info("DTO prepared: studentId=" + dashboardDTO.getStudentId() + 
                        ", programId=" + dashboardDTO.getProgramId() + 
                        ", currentSemester=" + dashboardDTO.getCurrentSemester() +
                        ", programName=" + dashboardDTO.getProgramName() +
                        ", Email=" + dashboardDTO.getEmail());

            req.setAttribute("dashboard", dashboardDTO);
            req.getRequestDispatcher("/studentDashboard.jsp").forward(req, resp);

        } catch (SQLException ex) {
            System.err.println("Database error in StudentDashboardServlet: " + ex.getMessage());
            ex.printStackTrace();
            throw new ServletException("Error retrieving student data.", ex);
        }
    }
}