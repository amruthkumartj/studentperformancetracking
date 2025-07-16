// src/main/java/com/portal/FetchProgramSemestersServlet.java
package com.portal.servlet;
import com.portal.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException; // Import SQLException
import java.util.List;

@WebServlet("/FetchProgramSemestersServlet")
public class FetchProgramSemestersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private ProgramDAO programDAO; // Declare ProgramDAO

    @Override
    public void init() throws ServletException {
        super.init();
        programDAO = new ProgramDAO(); // Initialize ProgramDAO
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JsonObject requestBody = gson.fromJson(sb.toString(), JsonObject.class);
            
            // Ensure programId is an integer as per your DAO
            int programId = -1;
            if (requestBody.has("programId") && !requestBody.get("programId").isJsonNull()) {
                programId = requestBody.get("programId").getAsInt();
            }

            System.out.println("DEBUG Servlet: Received request for semesters for programId: " + programId);

            if (programId == -1) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Program ID is required.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                List<Integer> semesters = programDAO.getSemestersByProgram(programId); // Fetch semesters from DB using DAO

                if (semesters != null && !semesters.isEmpty()) {
                    JsonArray semestersArray = new JsonArray();
                    for (Integer sem : semesters) {
                        semestersArray.add(sem);
                    }
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("semesters", semestersArray);
                    System.out.println("DEBUG Servlet: Fetched " + semesters.size() + " semesters for program " + programId + ".");
                } else {
                    jsonResponse.addProperty("status", "info"); // Use info for no semesters found
                    jsonResponse.addProperty("message", "No semesters found for program ID: " + programId);
                    System.out.println("DEBUG Servlet: No semesters found for program " + programId + ".");
                }
                out.print(gson.toJson(jsonResponse));
            }

        } catch (SQLException e) { // Catch SQLException
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error fetching semesters: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("ERROR Servlet: SQL Exception in FetchProgramSemestersServlet: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error fetching semesters: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("ERROR Servlet: General Exception in FetchProgramSemestersServlet: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }
}
