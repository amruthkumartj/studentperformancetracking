// New File: src/main/java/com/portal/servlet/FetchWeeklyTimetableServlet.java

package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.portal.Schedule;
import com.portal.ScheduleDAO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/FetchWeeklyTimetableServlet")
public class FetchWeeklyTimetableServlet extends HttpServlet {
    private ScheduleDAO scheduleDAO;
    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        scheduleDAO = new ScheduleDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        int programId = Integer.parseInt(request.getParameter("programId"));
        int semester = Integer.parseInt(request.getParameter("semester"));

        try {
            List<Schedule> weeklySchedules = scheduleDAO.getWeeklyTimetable(programId, semester);

            // Group schedules by day of the week
            Map<String, List<JsonObject>> groupedByDay = new LinkedHashMap<>();
            for (Schedule schedule : weeklySchedules) {
                String day = schedule.getCourseId(); // Retrieving the day stored in courseId
                groupedByDay.computeIfAbsent(day, k -> new ArrayList<>());

                JsonObject scheduleJson = new JsonObject();
                scheduleJson.addProperty("time", schedule.getClassTime().toString());
                scheduleJson.addProperty("subject", schedule.getSubjectName());
                scheduleJson.addProperty("location", schedule.getLocation());
                groupedByDay.get(day).add(scheduleJson);
            }

            PrintWriter out = response.getWriter();
            out.print(gson.toJson(groupedByDay));
            out.flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}