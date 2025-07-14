package com.portal.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.portal.User;
import com.portal.UserDAO;
import com.portal.FacultyDAO;
import com.portal.ProgramCourseDAO;

@WebServlet("/admin/approveFaculty")
public class AdminFacultyApprovalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private FacultyDAO facultyDAO;
    private ProgramCourseDAO programCourseDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        facultyDAO = new FacultyDAO();
        programCourseDAO = new ProgramCourseDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            if ("STUDENT".equalsIgnoreCase(currentUser.getRole())) {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
            } else if ("FACULTY".equalsIgnoreCase(currentUser.getRole())) {
                response.sendRedirect(request.getContextPath() + "/facultydashboard");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You are not authorized to view this page.");
            }
            return;
        }

        try {
            List<User> pendingFaculty = userDAO.getPendingFaculty();
            request.setAttribute("pendingFaculty", pendingFaculty);

            List<String[]> allPrograms = programCourseDAO.getAllPrograms();
            request.setAttribute("allPrograms", allPrograms);

        } catch (SQLException e) {
            System.err.println("Database error fetching data for admin approval: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "A database error occurred while loading faculty approval data.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while loading admin approval data: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        request.getRequestDispatcher("/adminApproveFaculty.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }
        User currentUser = (User) session.getAttribute("user");
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You are not authorized to perform this action.");
            return;
        }

        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");

        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            request.getSession().setAttribute("error", "User ID is required for approval action.");
            response.sendRedirect(request.getContextPath() + "/admin/approveFaculty");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);
            // Ensure UserDAO has getUserById() that returns a User object
            User userToApprove = userDAO.getUserById(userId);

            if (userToApprove == null) {
                request.getSession().setAttribute("error", "User not found with ID " + userId + ".");
                response.sendRedirect(request.getContextPath() + "/admin/approveFaculty");
                return;
            }

            if ("approve".equalsIgnoreCase(action)) {
                String designation = request.getParameter("designation");
                String[] selectedProgramIds = request.getParameterValues("programIds");

                if (designation == null || designation.trim().isEmpty()) {
                    request.getSession().setAttribute("error", "Designation is required for faculty approval.");
                    response.sendRedirect(request.getContextPath() + "/admin/approveFaculty");
                    return;
                }

                // 1. Approve user in 'users' table
                boolean userApproved = userDAO.approveFaculty(userId);

                if (userApproved) {
                    // 2. Add record to 'faculty' table
                    // CHANGED: Using userToApprove.getUsername() as the full_name
                    boolean facultyAdded = facultyDAO.addFaculty(userId, userToApprove.getUsername(), designation);

                    if (facultyAdded) {
                        int facultyId = facultyDAO.getFacultyIdByUserId(userId);

                        boolean programsAssigned = true;
                        if (selectedProgramIds != null && selectedProgramIds.length > 0) {
                            for (String programIdStr : selectedProgramIds) {
                                try {
                                    int programId = Integer.parseInt(programIdStr);
                                    if (!facultyDAO.assignProgramToFaculty(facultyId, programId)) {
                                        programsAssigned = false;
                                        System.err.println("Failed to assign program " + programId + " to faculty " + facultyId);
                                    }
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid program ID format: " + programIdStr);
                                    programsAssigned = false;
                                }
                            }
                        } else {
                            System.out.println("No programs selected for faculty " + userId);
                        }

                        if (programsAssigned) {
                            request.getSession().setAttribute("message", "Faculty " + userToApprove.getUsername() + " approved and programs assigned successfully.");
                        } else {
                            request.getSession().setAttribute("message", "Faculty " + userToApprove.getUsername() + " approved, but some program assignments failed.");
                        }
                    } else {
                        request.getSession().setAttribute("error", "Faculty " + userToApprove.getUsername() + " approved in users table, but failed to create faculty record. Contact support.");
                        System.err.println("CRITICAL: Failed to add faculty record for user_id " + userId);
                    }
                } else {
                    request.getSession().setAttribute("error", "Failed to approve user " + userToApprove.getUsername() + " in the users table.");
                }
            } else {
                request.getSession().setAttribute("error", "Invalid action specified.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Invalid User ID format.");
            System.err.println("Error parsing userId in AdminFacultyApprovalServlet: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            request.getSession().setAttribute("error", "A database error occurred during approval: " + e.getMessage());
            System.err.println("Database error in AdminFacultyApprovalServlet doPost: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            request.getSession().setAttribute("error", "An unexpected error occurred during approval: " + e.getMessage());
            System.err.println("Error in AdminFacultyApprovalServlet doPost: " + e.getMessage());
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/admin/approveFaculty");
    }
}