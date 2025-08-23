package com.portal.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.portal.User;
import com.portal.datatransfer_access.AIDAO; // Import the new AIDAO

@WebServlet("/AssistantServlet")
public class AssistantServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // AI Model Configuration
    private static final String PROJECT_ID = System.getenv("PROJECT_ID");
    private static final String LOCATION = System.getenv("LOCATION");
    private static final String MODEL_NAME = System.getenv("MODEL_NAME");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get user question and session info
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JsonObject jsonPayload = new Gson().fromJson(requestBody, JsonObject.class);
        String userQuestion = jsonPayload.get("question").getAsString().toLowerCase();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated.");
            return;
        }
        User user = (User) session.getAttribute("user");

        String contextData = "";
        
        try {
            // 2. Delegate all logic to the AIDAO to get the context
            AIDAO aiDAO = new AIDAO();
            contextData = aiDAO.getContextForQuery(userQuestion, user);

        } catch (SQLException e) {
            e.printStackTrace(); // Log the full error to the server console
            contextData = "An error occurred while accessing the database.";
        } catch (Exception e) {
            e.printStackTrace();
            contextData = "An unexpected application error occurred.";
        }

        // 3. Construct the final prompt for the AI
        String finalPrompt;
        // If contextData is empty, it means it's not a portal query, so we just send the raw question.
        if (!contextData.isEmpty()) {
            finalPrompt = "You are a helpful faculty assistant for a college portal. "
                       + "Answer the user's question based ONLY on the provided context. "
                       + "If the context says an error occurred, state that clearly. "
                       + "If the context is 'Access Denied', you MUST inform the user they lack permission. "
                       + "Be concise and clear.\n\n"
                       + "--- Provided Context ---\n"
                       + contextData + "\n"
                       + "--- End Context ---\n\n"
                       + "User Question: " + userQuestion;
        } else {
            finalPrompt = userQuestion; // It's a general question, not portal-related
        }

        // 4. Call the Gemini API
        String geminiResponse;
        try (VertexAI vertexAi = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAi);
            GenerateContentResponse apiResponse = model.generateContent(finalPrompt);
            geminiResponse = ResponseHandler.getText(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            geminiResponse = "Sorry, I encountered an error while contacting the AI model.";
        }

        // 5. Send the response to the frontend
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(geminiResponse);
    }
}
