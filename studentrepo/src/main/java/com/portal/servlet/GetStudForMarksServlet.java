package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder; // Import GsonBuilder
import com.google.gson.JsonObject;
import com.portal.MarksDAO; // Assuming MarksDAO is in com.portal
import com.portal.StudentMarkEntryDTO; // CORRECTED: Import from com.portal package

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetStudForMarks")
public class GetStudForMarksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // CORRECTED: Initialize Gson with serializeNulls()
    private Gson gson = new GsonBuilder().serializeNulls().create(); // This is the crucial change

    public GetStudForMarksServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("This servlet only supports POST requests for fetching student marks data.");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try (BufferedReader reader = request.getReader()) {
            JsonObject jsonInput = gson.fromJson(reader, JsonObject.class);

            int programId = jsonInput.get("programId").getAsInt();
            int semester = jsonInput.get("semester").getAsInt();
            String courseId = jsonInput.get("courseId").getAsString();
            String examType = jsonInput.get("examType").getAsString();

            MarksDAO marksDAO = new MarksDAO();
            List<StudentMarkEntryDTO> studentsWithMarks = marksDAO.getStudentsForMarksEntry(programId, semester, courseId, examType);

            // The 'gson' instance now correctly handles nulls because of the change above
            String jsonResponse = gson.toJson(studentsWithMarks);
            out.print(jsonResponse);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace(); // This is crucial for debugging. It should print to Tomcat's console.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Failed to retrieve student marks data: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
            out.flush();
        }
    }
}