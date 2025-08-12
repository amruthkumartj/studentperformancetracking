package com.portal.servlet;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// --- IMPORTANT: Import necessary libraries ---
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/AssistantServlet")
public class AssistantServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // --- Configuration is now read from Environment Variables ---
    private static final String PROJECT_ID = System.getenv("PROJECT_ID");
    private static final String LOCATION = System.getenv("LOCATION");
    private static final String MODEL_NAME = System.getenv("MODEL_NAME");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get the JSON payload from the request
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Gson gson = new Gson();
        JsonObject jsonPayload = gson.fromJson(requestBody, JsonObject.class);
        String userQuestion = jsonPayload.get("question").getAsString();

        String contextData = "";

        // 2. --- CONTEXTUAL DATA FETCHING LOGIC ---
        String lowerCaseQuestion = userQuestion.toLowerCase();
        if (lowerCaseQuestion.contains("student details") || lowerCaseQuestion.contains("marks for") || lowerCaseQuestion.contains("attendance for")) {
            // TODO: Implement a method to parse the student ID from the question
            int studentId = 123; // Dummy ID

            // TODO: Use your DAOs to get real data.
            contextData = "Data for student ID " + studentId + ": Name: Amruth K, Program: Computer Science, Semester: 4. "
                        + "Marks: Java=85, Database=92. Attendance: 91%.";

        } else {
            contextData = "No specific student data requested.";
        }


        // 3. Create the final prompt for the AI
        String finalPrompt = "You are a helpful faculty assistant for a college portal. "
                           + "Answer the user's question based on the provided context. "
                           + "If the question is general and not related to the context, answer it normally. "
                           + "Be concise and clear.\n\n"
                           + "--- Provided Context ---\n"
                           + contextData + "\n"
                           + "--- End Context ---\n\n"
                           + "User Question: " + userQuestion;

        String geminiResponse;

        // 4. Call the Gemini API using Application Default Credentials
        // The library will automatically find the Secret File you created in Render.
        try (VertexAI vertexAi = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAi);
            GenerateContentResponse apiResponse = model.generateContent(finalPrompt);
            geminiResponse = ResponseHandler.getText(apiResponse);
        } catch (Exception e) {
            e.printStackTrace(); // Log the actual error to your server logs
            geminiResponse = "Sorry, I encountered an error while contacting the AI model. Please check the server logs. Details: " + e.getMessage();
        }

        // 5. Send the AI's answer back to the browser
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(geminiResponse);
    }
}