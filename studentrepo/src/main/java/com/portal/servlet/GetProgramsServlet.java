package com.portal.servlet;

import com.google.gson.Gson; // Import Gson for JSON conversion
import com.portal.ProgramCourseDAO; // Assuming your DAO is in com.portal
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetProgramsServlet")
public class GetProgramsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProgramCourseDAO programCourseDAO;
    private Gson gson; // Gson instance for JSON serialization

    @Override
    public void init() throws ServletException {
        super.init();
        programCourseDAO = new ProgramCourseDAO();
        gson = new Gson(); // Initialize Gson
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // This servlet will return a JSON array of programs
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<String[]> programs = programCourseDAO.getAllPrograms();
            // Convert the list of String arrays to JSON and write to response
            response.getWriter().write(gson.toJson(programs));
            System.out.println("DEBUG: GetProgramsServlet - Sent " + programs.size() + " programs.");
        } catch (Exception e) {
            System.err.println("❌ Error in GetProgramsServlet: " + e.getMessage());
            e.printStackTrace();
            // Send an error response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Error fetching programs: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Allow POST requests to be handled by doGet for simplicity
        doGet(request, response);
    }
}
