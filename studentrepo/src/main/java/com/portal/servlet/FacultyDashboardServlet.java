package com.portal.servlet;

import com.portal.User;
import com.portal.datatransfer_access.FacultyDAO;
import com.portal.datatransfer_access.ProgramCourseDAO;
import com.portal.datatransfer_access.ProgramDAO;
import com.portal.Program;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.google.gson.Gson; // <--- MAKE SURE YOU HAVE THIS IMPORT

@WebServlet("/facultydashboard")
public class FacultyDashboardServlet extends HttpServlet {
    private FacultyDAO facultyDAO;
    private ProgramCourseDAO programCourseDAO;
    private ProgramDAO programDAO;
    private Gson gson; // <--- AND THIS DECLARATION

    @Override
    public void init() throws ServletException {
        super.init();
        facultyDAO = new FacultyDAO();
        programCourseDAO = new ProgramCourseDAO();
        programDAO = new ProgramDAO();
        gson = new Gson(); // <--- AND THIS INITIALIZATION
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (!"FACULTY".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not authorized to view this page.");
            return;
        }

        try {
            int userId = currentUser.getId();
            int facultyId = facultyDAO.getFacultyIdByUserId(userId);

            if (facultyId == -1) {
                System.err.println("Faculty user_id " + userId + " does not have a corresponding faculty_id.");
                req.setAttribute("error", "Faculty profile incomplete. Please contact support.");
                req.getRequestDispatcher("/facultyDashboard.jsp").forward(req, resp);
                return;
            }

            List<String[]> assignedProgramsData = facultyDAO.getAssignedPrograms(facultyId);
            Map<String, Integer> programStudentCounts = new HashMap<>();

            int totalOverallStudents = 0;
            int totalAssignedCourses = 0;

            Set<Integer> assignedProgramIds = new HashSet<>();

            if (assignedProgramsData != null && !assignedProgramsData.isEmpty()) {
                for (String[] programArray : assignedProgramsData) {
                    try {
                        int programId = Integer.parseInt(programArray[0]);
                        String programName = programArray[1];
                        assignedProgramIds.add(programId);

                        int studentCount = programCourseDAO.getTotalStudentsInProgram(programId);
                        programStudentCounts.put(programName, studentCount);
                        totalOverallStudents += studentCount;

                        int coursesInProgram = programCourseDAO.getTotalCoursesInProgram(programId);
                        totalAssignedCourses += coursesInProgram;

                    } catch (NumberFormatException nfe) {
                        System.err.println("Invalid program ID format encountered from assignedPrograms: " + programArray[0]);
                    }
                }
            } else {
                System.out.println("DEBUG: No programs explicitly assigned to facultyId: " + facultyId);
            }

            List<Program> allPrograms = programDAO.getAllPrograms();

            // --- NEW/VERIFIED: Serialize data to JSON for JavaScript consumption ---
            String allProgramsJson = gson.toJson(allPrograms);
            String assignedProgramIdsJson = gson.toJson(assignedProgramIds);

            // Set attributes for the JSP
            req.setAttribute("assignedProgramsWithCounts", programStudentCounts);
            req.setAttribute("totalOverallStudents", totalOverallStudents);
            req.setAttribute("totalAssignedCourses", totalAssignedCourses);
            req.setAttribute("assignedProgramIds", assignedProgramIds); // Still useful for server-side JSP logic if any
            req.setAttribute("allPrograms", allPrograms); // Still useful for server-side JSP logic if any

            // NEW/VERIFIED: Set JSON strings as attributes for client-side JavaScript
            req.setAttribute("allProgramsJson", allProgramsJson);
            req.setAttribute("assignedProgramIdsJson", assignedProgramIdsJson);
            Map<String, String> profileDetails = facultyDAO.getFacultyProfileDetails(userId);
            // Pass these details to the JSP page
            req.setAttribute("profileDetails", profileDetails);

            req.getRequestDispatcher("/facultyDashboard.jsp").forward(req, resp);

        } catch (SQLException e) {
            System.err.println("Database error in FacultyDashboardServlet: " + e.getMessage());
            e.printStackTrace();
            req.setAttribute("error", "Database error: " + e.getMessage());
            req.getRequestDispatcher("/facultyDashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred in FacultyDashboardServlet: " + e.getMessage());
            e.printStackTrace();
            req.setAttribute("error", "An unexpected error occurred: " + e.getMessage());
            req.getRequestDispatcher("/facultyDashboard.jsp").forward(req, resp);
        }
    }
}