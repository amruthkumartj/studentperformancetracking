// src/main/java/com/portal/servlet/GetAttendanceRecordsServlet.java
package com.portal.studservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection; // Keep import for DBUtil.getConnection() if used elsewhere, but not for DAO constructor
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fatboyindustrial.gsonjavatime.LocalDateConverter;
import com.fatboyindustrial.gsonjavatime.LocalDateTimeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.portal.AttendanceRecord;
import com.portal.DBUtil; // Keep this import as DAOs use it
import com.portal.User;
import com.portal.datatransfer_access.AttendanceDAO;

@WebServlet("/GetAttendanceRecordsServlet")
public class GetAttendanceRecordsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson;
    private AttendanceDAO attendanceDAO; // Declare DAO instance

    @Override
    public void init() throws ServletException {
        super.init();
        // CORRECTED GSONBuilder initialization for com.fatboyindustrial.gson-javatime-serialisers
        gson = new GsonBuilder()
                // Register converters for specific java.time types you use in your models
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeConverter())
                .registerTypeAdapter(java.time.LocalDate.class, new LocalDateConverter())
                // Keep this for java.util.Date if still relevant for your filter parsing,
                // but generally, if you're moving to java.time, you should transition all date handling.
                .setDateFormat("yyyy-MM-dd")
                .create();
        
        // Initialize AttendanceDAO here, it no longer needs a Connection in its constructor
        attendanceDAO = new AttendanceDAO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User loggedInUser = null;
        if (session != null) {
            loggedInUser = (User) session.getAttribute("user");
        }

        if (loggedInUser == null || (!"admin".equalsIgnoreCase(loggedInUser.getRole()) && !"faculty".equalsIgnoreCase(loggedInUser.getRole()))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access to view attendance records. Please log in as an Admin or Faculty.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        // Connection conn = null; // No longer needed here as DAO manages its own connections
        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("DEBUG: GetAttendanceRecordsServlet received request body: " + requestBody);

            JsonObject requestData;
            try {
                requestData = gson.fromJson(requestBody, JsonObject.class);
                if (requestData == null) {
                    throw new JsonSyntaxException("Request body is empty or malformed JSON.");
                }
            } catch (JsonSyntaxException e) {
                System.err.println("JSON parsing error in GetAttendanceRecordsServlet: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid JSON format in request: " + e.getMessage());
                out.print(gson.toJson(jsonResponse));
                return;
            }

            int programId = requestData.has("programId") && !requestData.get("programId").isJsonNull() ? requestData.get("programId").getAsInt() : -1;
            int semester = requestData.has("semester") && !requestData.get("semester").isJsonNull() ? requestData.get("semester").getAsInt() : -1;

            String subjectId = requestData.has("subjectId") && !requestData.get("subjectId").isJsonNull() ? requestData.get("subjectId").getAsString() : null;
            if (subjectId != null && (subjectId.equals("-1") || subjectId.trim().isEmpty())) {
                subjectId = null;
            }

            String studentSearch = requestData.has("studentSearch") && !requestData.get("studentSearch").isJsonNull() ? requestData.get("studentSearch").getAsString() : null;


            Date dateFilter = null; // This is java.util.Date
            if (requestData.has("date") && !requestData.get("date").isJsonNull()) {
                String dateString = requestData.get("date").getAsString();
                if (!dateString.isEmpty()) {
                    try {
                        dateFilter = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                    } catch (ParseException e) {
                        System.err.println("Invalid date format received for filter: '" + dateString + "' - " + e.getMessage());
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Invalid date format provided for filter: " + dateString);
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(jsonResponse));
                        return;
                    }
                }
            }

            // No longer need to pass connection, DAO gets it internally
            List<AttendanceRecord> attendanceRecords = attendanceDAO.getAttendanceRecords(
                programId, semester, subjectId, dateFilter, studentSearch);

            jsonResponse.addProperty("status", "success");
            jsonResponse.add("records", gson.toJsonTree(attendanceRecords)); // Wrap list in a JSON object
            out.print(gson.toJson(jsonResponse));

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQLException in GetAttendanceRecordsServlet: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error fetching attendance records: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error in GetAttendanceRecordsServlet: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "An unexpected error occurred: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } finally {
            // No need to close connection here, DBUtil manages the pool
            out.flush(); // Ensure all buffered output is sent
        }
    }
}
