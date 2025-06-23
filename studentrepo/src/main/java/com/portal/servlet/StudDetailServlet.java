package com.portal.servlet;
import java.io.BufferedReader; // Import BufferedReader
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson; // Import Gson
import com.google.gson.JsonObject; // Import JsonObject if you plan to read JSON input
import com.portal.Student;
import com.portal.UserDAO;

// Using an annotation to map the servlet. This is easier than web.xml.
// The URL '/GetStudentsServlet' matches the one in our JavaScript.
@WebServlet("/GetStudentsServlet")
public class StudDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    // Changed from doGet to doPost
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // --- If you were expecting parameters in the request body, you'd read them here ---
        // For example, if you wanted to read a JSON object sent from the client:
        /*
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String jsonInput = sb.toString();

        // If jsonInput is not empty, you can parse it
        if (!jsonInput.isEmpty()) {
            JsonObject inputJson = gson.fromJson(jsonInput, JsonObject.class);
            // Now you can extract parameters, e.g., String studentId = inputJson.get("studentId").getAsString();
            // Or if you sent a simple key-value pair, you'd parse differently.
        }
        */
        // --- End of parameter reading section ---

        // 1. Get the list of students from the DAO
        UserDAO userDAO = new UserDAO();
        List<Student> students = userDAO.getAllStudents();

        // 2. Convert the list to a JSON string
        String studentsJsonString = this.gson.toJson(students);

        // 3. Send the JSON as the response
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(studentsJsonString);
        out.flush();
    }

    // It's good practice to either remove doGet if it's no longer used,
    // or provide a response indicating that GET is not supported.
    // For this conversion, I'm keeping it commented out or you could remove it.
    /*
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method is not supported for this endpoint. Please use POST.");
    }
    */
}