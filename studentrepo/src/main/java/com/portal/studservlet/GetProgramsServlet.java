package com.portal.studservlet;

import com.google.gson.Gson;
import com.portal.User; // Make sure this import is correct
import com.portal.datatransfer_access.FacultyDAO; // Make sure this import is correct
import com.portal.datatransfer_access.ProgramCourseDAO; // Make sure this import is correct
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/GetProgramsServlet")
public class GetProgramsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        // Security check: Ensure a user is logged in
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Not authenticated\"}");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. Get the currently logged-in faculty's ID
            User currentUser = (User) session.getAttribute("user");
            FacultyDAO facultyDAO = new FacultyDAO();
            int facultyId = facultyDAO.getFacultyIdByUserId(currentUser.getId());

            // 2. Get the list of ALL programs (like you did before)
            ProgramCourseDAO programCourseDAO = new ProgramCourseDAO();
            List<String[]> allPrograms = programCourseDAO.getAllPrograms();

            // 3. Get the list of ASSIGNED program IDs for that faculty
            List<String[]> assignedProgramsData = facultyDAO.getAssignedPrograms(facultyId);
            Set<Integer> assignedProgramIds = new HashSet<>();
            if (assignedProgramsData != null) {
                for (String[] programArray : assignedProgramsData) {
                    assignedProgramIds.add(Integer.parseInt(programArray[0]));
                }
            }

            // 4. Combine both lists into a single response object
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("allPrograms", allPrograms);
            responseData.put("assignedProgramIds", assignedProgramIds);

            // 5. Send the combined data as a single JSON response
            new Gson().toJson(responseData, response.getWriter());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error fetching program data.\"}");
            e.printStackTrace();
        }
    }
}