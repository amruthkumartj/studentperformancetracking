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
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger; // Import the Logger class

@WebServlet("/FetchDailySchedulesServlet")
public class FetchDailySchedulesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ScheduleDAO scheduleDAO;
    private Gson gson;
    
    // Create a logger instance for debugging
    private static final Logger LOGGER = Logger.getLogger(FetchDailySchedulesServlet.class.getName());

    @Override
    public void init() throws ServletException {
        scheduleDAO = new ScheduleDAO();
        gson = new Gson();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");
        
        // Log the user's role to confirm a student is accessing the servlet
        if (currentUser != null) {
            LOGGER.info("User accessed FetchDailySchedulesServlet. Role: " + currentUser.getRole());
        } else {
            LOGGER.warning("Unauthorized access attempt. User session is null.");
        }

        if (currentUser == null || (!"FACULTY".equalsIgnoreCase(currentUser.getRole()) 
                                   && !"ADMIN".equalsIgnoreCase(currentUser.getRole()) 
                                   && !"STUDENT".equalsIgnoreCase(currentUser.getRole()))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        String programIdStr = request.getParameter("programId");
        String semesterStr = request.getParameter("semester");
        String dateStr = request.getParameter("date");
        
        // Log the incoming parameters to check if they are correct
        LOGGER.info("Received parameters: programId=" + programIdStr + ", semester=" + semesterStr + ", date=" + dateStr);

        if (programIdStr == null || semesterStr == null || dateStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\", \"message\":\"Missing parameters.\"}");
            return;
        }

        try {
            int programId = Integer.parseInt(programIdStr);
            int semester = Integer.parseInt(semesterStr);
            LocalDate date = LocalDate.parse(dateStr);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            
            LOGGER.info("Parsed values: programId=" + programId + ", semester=" + semester + ", dayOfWeek=" + dayOfWeek);

            // 1. Get Regular Timetable for the day of the week
            List<Schedule> regularSchedules = scheduleDAO.getRegularTimetable(programId, semester, dayOfWeek);
            LOGGER.info("Regular schedules found: " + regularSchedules.size());

            // 2. Get Extra Classes for that specific date
            List<Schedule> extraSchedules = scheduleDAO.getDailySchedules(programId, semester, date);
            LOGGER.info("Extra schedules found: " + extraSchedules.size());

            // 3. Combine them into a single JSON response
            JsonArray schedulesJson = new JsonArray();

            // Add regular schedules with a "type" flag
            for (Schedule schedule : regularSchedules) {
                JsonObject scheduleObj = new JsonObject();
                scheduleObj.addProperty("type", "regular");
                scheduleObj.addProperty("subjectName", schedule.getSubjectName());
                scheduleObj.addProperty("time", schedule.getClassTime().toString());
                scheduleObj.addProperty("location", schedule.getLocation());
                schedulesJson.add(scheduleObj);
            }

            // Add extra schedules with a "type" flag and more details
            for (Schedule schedule : extraSchedules) {
                JsonObject scheduleObj = new JsonObject();
                scheduleObj.addProperty("type", "extra");
                scheduleObj.addProperty("id", schedule.getScheduleId());
                scheduleObj.addProperty("subjectName", schedule.getSubjectName());
                scheduleObj.addProperty("date", schedule.getClassDate().toString());
                scheduleObj.addProperty("time", schedule.getClassTime().toString());
                scheduleObj.addProperty("location", schedule.getLocation());
                schedulesJson.add(scheduleObj);
            }

            jsonResponse.addProperty("status", "success");
            jsonResponse.add("schedules", schedulesJson);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}