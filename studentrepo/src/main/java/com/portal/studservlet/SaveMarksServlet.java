package com.portal.studservlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.portal.datatransfer_access.MarksDAO;
import com.portal.datatransfer_access.StudentMarkEntryDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/SaveMarksServlet")
public class SaveMarksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    public SaveMarksServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("This servlet only supports POST requests for saving marks.");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try (BufferedReader reader = request.getReader()) {
            Type listType = new TypeToken<List<StudentMarkEntryDTO>>() {}.getType();
            List<StudentMarkEntryDTO> marksEntries = gson.fromJson(reader, listType);

            if (marksEntries == null || marksEntries.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("status", "error");
                errorResponse.addProperty("message", "No marks data provided for submission.");
                out.print(gson.toJson(errorResponse));
                out.flush();
                return;
            }

            MarksDAO marksDAO = new MarksDAO();
            marksDAO.saveMarks(marksEntries); // Call saveMarks with only the list of DTOs

            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("status", "success");
            successResponse.addProperty("message", "Marks submitted successfully!");
            out.print(gson.toJson(successResponse));
            out.flush();

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Database error during marks submission: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Failed to process marks submission: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
            out.flush();
        }
    }
}
