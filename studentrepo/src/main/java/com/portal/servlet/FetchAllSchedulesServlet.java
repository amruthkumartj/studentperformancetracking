// src/main/java/com/portal/servlet/FetchAllSchedulesServlet.java

package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.portal.Schedule;
import com.portal.User;
import com.portal.datatransfer_access.ScheduleDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@WebServlet("/FetchAllSchedulesServlet")
public class FetchAllSchedulesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ScheduleDAO scheduleDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        scheduleDAO = new ScheduleDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || (!"FACULTY".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        String programIdStr = request.getParameter("programId");
        String semesterStr = request.getParameter("semester");

        if (programIdStr == null || semesterStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\", \"message\":\"Missing parameters.\"}");
            return;
        }

        try {
            int programId = Integer.parseInt(programIdStr);
            int semester = Integer.parseInt(semesterStr);

            // Fetch both regular and extra classes in a single request
            List<Schedule> regularSchedules = scheduleDAO.getWeeklyTimetable(programId, semester);
            List<Schedule> extraSchedules = scheduleDAO.getAllExtraSchedules(programId, semester);

            // Combine and process schedules for the frontend
            Map<DayOfWeek, List<JsonObject>> weeklySchedulesMap = new LinkedHashMap<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                weeklySchedulesMap.put(day, new ArrayList<>());
            }

            // Process regular schedules
            for (Schedule s : regularSchedules) {
                DayOfWeek day = DayOfWeek.valueOf(s.getCourseId()); // 'courseId' field is repurposed for day
                JsonObject regularJson = new JsonObject();
                regularJson.addProperty("type", "regular");
                regularJson.addProperty("time", s.getClassTime().toString());
                regularJson.addProperty("subjectName", s.getSubjectName());
                regularJson.addProperty("location", s.getLocation());
                weeklySchedulesMap.get(day).add(regularJson);
            }

            // Process extra schedules
            for (Schedule s : extraSchedules) {
                DayOfWeek day = s.getClassDate().getDayOfWeek();
                JsonObject extraJson = new JsonObject();
                extraJson.addProperty("type", "extra");
                extraJson.addProperty("id", s.getScheduleId());
                extraJson.addProperty("time", s.getClassTime().toString());
                extraJson.addProperty("subjectName", s.getSubjectName());
                extraJson.addProperty("location", s.getLocation());
                extraJson.addProperty("isRecurring", s.isRecurring());
                if (s.isRecurring() && s.getRecurrenceEndDate() != null) {
                    extraJson.addProperty("classDate", s.getClassDate().toString());
                    extraJson.addProperty("recurrenceEndDate", s.getRecurrenceEndDate().toString());
                }
                weeklySchedulesMap.get(day).add(extraJson);
            }

            // Sort schedules by time within each day
            for (List<JsonObject> dailyList : weeklySchedulesMap.values()) {
                dailyList.sort((a, b) -> a.get("time").getAsString().compareTo(b.get("time").getAsString()));
            }

            jsonResponse.addProperty("status", "success");
            jsonResponse.add("schedules", gson.toJsonTree(weeklySchedulesMap));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error fetching weekly schedules: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}