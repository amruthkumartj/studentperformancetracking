// FINAL AND COMPLETE AddDailyScheduleServlet.java with Recurring Conflict Warning

package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.portal.User;
import com.portal.datatransfer_access.FacultyDAO;
import com.portal.datatransfer_access.ScheduleDAO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/AddDailyScheduleServlet")
public class AddDailyScheduleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ScheduleDAO scheduleDAO;
    private FacultyDAO facultyDAO;
    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // This line MUST be first.
        scheduleDAO = new ScheduleDAO();
        facultyDAO = new FacultyDAO();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        HttpSession session = request.getSession(false);

        try {
            User currentUser = (User) (session != null ? session.getAttribute("user") : null);

            if (currentUser == null || (!"FACULTY".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unauthorized access. Please log in again.");
            } else {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) { sb.append(line); }
                }
                JsonObject requestData = gson.fromJson(sb.toString(), JsonObject.class);

                // === PATH 1: User has clicked "Save Non-Conflicting Dates" ===
                if (requestData.has("forceConfirmation") && requestData.get("forceConfirmation").getAsBoolean()) {
                    // Retrieve the saved valid dates and original data from the session
                    List<LocalDate> validDates = (List<LocalDate>) session.getAttribute("pendingValidDates");
                    String originalDataJson = (String) session.getAttribute("pendingScheduleData");
                    JsonObject originalData = gson.fromJson(originalDataJson, JsonObject.class);
                    
                    if (validDates == null || originalData == null) {
                        throw new IllegalStateException("No pending schedule found in session to confirm.");
                    }

                    // Parse original data again
                    int facultyId = facultyDAO.getFacultyIdByUserId(currentUser.getId());
                    int programId = originalData.get("programId").getAsInt();
                    int semester = originalData.get("semester").getAsInt();
                    String subjectName = originalData.get("subjectName").getAsString();
                    LocalTime classTime = LocalTime.parse(originalData.get("classTime").getAsString());
                    int durationMinutes = originalData.get("durationMinutes").getAsInt();
                    String location = originalData.get("roomLocation").getAsString();
                    boolean isRecurring = originalData.has("isRecurring");
                    LocalDate recurrenceEndDate = isRecurring && originalData.has("endDate") && !originalData.get("endDate").isJsonNull()
                                              ? LocalDate.parse(originalData.get("endDate").getAsString()) : null;

                    // Use the new DAO method to save only the valid dates
                    if (scheduleDAO.addDailySchedulesForDates(facultyId, programId, semester, null, subjectName, classTime, durationMinutes, location, isRecurring, recurrenceEndDate, validDates)) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.addProperty("message", "Successfully scheduled classes for " + validDates.size() + " valid date(s).");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "An error occurred while saving the valid schedule dates.");
                    }
                    // Clean up session attributes
                    session.removeAttribute("pendingValidDates");
                    session.removeAttribute("pendingScheduleData");

                } else {
                    // === PATH 2: This is the initial "Schedule Class" request ===
                    int programId = requestData.get("programId").getAsInt();
                    int semester = requestData.get("semester").getAsInt();
                    LocalDate classDate = LocalDate.parse(requestData.get("classDate").getAsString());
                    LocalTime classTime = LocalTime.parse(requestData.get("classTime").getAsString());
                    int durationMinutes = requestData.get("durationMinutes").getAsInt();
                    LocalTime newEndTime = classTime.plusMinutes(durationMinutes);
                    boolean isRecurring = requestData.has("isRecurring");
                    LocalDate recurrenceEndDate = isRecurring && requestData.has("endDate") && !requestData.get("endDate").isJsonNull()
                                              ? LocalDate.parse(requestData.get("endDate").getAsString()) : classDate;
                    
                    List<String> conflictingDates = new ArrayList<>();
                    List<LocalDate> validDatesToSchedule = new ArrayList<>();

                    for (LocalDate date = classDate; !date.isAfter(recurrenceEndDate); date = date.plusDays(1)) {
                        DayOfWeek dayOfWeek = date.getDayOfWeek();
                        if (scheduleDAO.countRegularConflicts(programId, semester, dayOfWeek, classTime, newEndTime) > 0 ||
                            scheduleDAO.countExtraConflicts(programId, semester, date, classTime, newEndTime) > 0) {
                            conflictingDates.add(date.toString());
                        } else {
                            validDatesToSchedule.add(date);
                        }
                    }
                    
                    if (!conflictingDates.isEmpty()) {
                        if (validDatesToSchedule.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            jsonResponse.addProperty("status", "error");
                            jsonResponse.addProperty("message", "All dates in the selected range conflict with existing schedules.");
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            jsonResponse.addProperty("status", "warning");
                            String conflictDatesString = String.join(", ", conflictingDates);
                            jsonResponse.addProperty("message", "Conflicts found on: " + conflictDatesString + ". Proceed to schedule on the " + validDatesToSchedule.size() + " non-conflicting day(s)?");
                            session.setAttribute("pendingScheduleData", requestData.toString());
                            session.setAttribute("pendingValidDates", validDatesToSchedule);
                        }
                    } else {
                        // No conflicts, save all dates
                        int facultyId = facultyDAO.getFacultyIdByUserId(currentUser.getId());
                        if (scheduleDAO.addDailySchedulesForDates(facultyId, programId, semester, null, requestData.get("subjectName").getAsString(), classTime, durationMinutes, requestData.get("roomLocation").getAsString(), isRecurring, recurrenceEndDate, validDatesToSchedule)) {
                            jsonResponse.addProperty("status", "success");
                            jsonResponse.addProperty("message", "Class(es) scheduled successfully.");
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            jsonResponse.addProperty("status", "error");
                            jsonResponse.addProperty("message", "Failed to add schedule to the database.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}