// src/main/java/com/portal/FetchProgramsServlet.java
package com.portal.servlet; // Changed package to com.portal
import com.portal.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException; // Import SQLException
import java.util.List;

@WebServlet("/FetchProgramsServlet")
public class FetchProgramsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private ProgramDAO programDAO; // Declare ProgramDAO

    @Override
    public void init() throws ServletException {
        super.init();
        programDAO = new ProgramDAO(); // Initialize ProgramDAO
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        JsonObject jsonResponse = new JsonObject();
        JsonArray programsArray = new JsonArray();

        try {
            List<Program> programs = programDAO.getAllPrograms(); // Fetch programs from DB using DAO

            if (programs != null && !programs.isEmpty()) {
                for (Program p : programs) {
                    JsonObject programObj = new JsonObject();
                    programObj.addProperty("id", p.getProgramId()); // Use getProgramId()
                    programObj.addProperty("name", p.getProgramName()); // Use getProgramName()
                    programsArray.add(programObj);
                }
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("programs", programsArray);
                System.out.println("DEBUG Servlet: Fetched " + programs.size() + " programs from DB.");
            } else {
                jsonResponse.addProperty("status", "info");
                jsonResponse.addProperty("message", "No programs found in the database.");
                System.out.println("DEBUG Servlet: No programs found in DB.");
            }
            out.print(gson.toJson(jsonResponse));

        } catch (SQLException e) { // Catch SQLException
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error fetching programs: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("ERROR Servlet: SQL Exception in FetchProgramsServlet: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error fetching programs: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("ERROR Servlet: General Exception in FetchProgramsServlet: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }
}
