// src/main/java/com/portal/servlet/DeleteScheduleServlet.java
package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.portal.User;
import com.portal.datatransfer_access.ScheduleDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import com.google.gson.JsonSyntaxException;

@WebServlet("/DeleteScheduleServlet")
public class DeleteScheduleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ScheduleDAO scheduleDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        scheduleDAO = new ScheduleDAO();
        gson = new Gson();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !"FACULTY".equalsIgnoreCase(currentUser.getRole())) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        try {
            JsonObject requestData = gson.fromJson(sb.toString(), JsonObject.class);
            int scheduleId = requestData.get("scheduleId").getAsInt();

            if (scheduleDAO.deleteSchedule(scheduleId)) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Schedule deleted successfully.");
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to delete schedule.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (JsonSyntaxException | NumberFormatException | SQLException e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing data: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}