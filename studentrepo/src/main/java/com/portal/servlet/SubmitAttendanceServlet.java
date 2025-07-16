// src/main/java/com/portal/servlet/SubmitAttendanceServlet.java
package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException; // Import for JsonSyntaxException
import com.portal.AttendanceDAO;
import com.portal.AttendanceRecord;
import com.portal.AttendanceSession;
import com.portal.DBUtil; // Keep this import as DAOs use it

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // For request body collection

@WebServlet("/SubmitAttendanceServlet")
public class SubmitAttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();
    private AttendanceDAO attendanceDAO; // Declare DAO instance

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize AttendanceDAO here, it no longer needs a Connection in its constructor
        attendanceDAO = new AttendanceDAO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        // User loggedInUser = null; // Assuming user validation is done in a filter or earlier servlet for this endpoint
        // if (session != null) {
        //     loggedInUser = (User) session.getAttribute("user");
        // }

        // Optional: Add user authentication/authorization check if needed for this servlet
        // if (loggedInUser == null || (!"faculty".equalsIgnoreCase(loggedInUser.getRole()) && !"admin".equalsIgnoreCase(loggedInUser.getRole()))) {
        //     jsonResponse.addProperty("status", "error");
        //     jsonResponse.addProperty("message", "Unauthorized access.");
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     out.print(gson.toJson(jsonResponse));
        //     return;
        // }

        try (Connection conn = DBUtil.getConnection()) { // Use try-with-resources for connection
            conn.setAutoCommit(false); // Start transaction

            // AttendanceDAO attendanceDAO = new AttendanceDAO(conn); // No longer needed here, initialized in init()

            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("DEBUG: SubmitAttendanceServlet received request body: " + requestBody);

            JsonObject requestData;
            try {
                requestData = gson.fromJson(requestBody, JsonObject.class);
                if (requestData == null) {
                    throw new JsonSyntaxException("Request body is empty or malformed JSON.");
                }
            } catch (JsonSyntaxException e) {
                System.err.println("JSON parsing error in SubmitAttendanceServlet: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid JSON format in request: " + e.getMessage());
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            int sessionIdFromRequest = requestData.get("sessionId").getAsInt();
            JsonArray recordsArray = requestData.getAsJsonArray("records");

            HttpSession httpSession = request.getSession(false);
            AttendanceSession currentSession = null;
            if (httpSession != null) {
                // Ensure the object stored in session is of the NEW AttendanceSession type
                currentSession = (AttendanceSession) httpSession.getAttribute("currentAttendanceSession");
            }

            if (currentSession == null || currentSession.getSessionId() != sessionIdFromRequest) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid or expired attendance session. Session ID mismatch or not found in session.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            String courseIdForAttendanceRecords = currentSession.getCourseId();

            if (courseIdForAttendanceRecords == null || courseIdForAttendanceRecords.isEmpty()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Course ID not found in the current attendance session. Cannot submit attendance.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            System.out.println("--- SubmitAttendanceServlet Debugging ---");
            System.out.println("Session ID from Request: " + sessionIdFromRequest);
            System.out.println("Session ID from currentSession Object: " + currentSession.getSessionId());
            System.out.println("Course ID from Session (for attendance records): " + courseIdForAttendanceRecords);
            System.out.println("Session Start Time: " + currentSession.getSessionStartTime()); // This will now print an Instant

            int academicYear = Year.now().getValue(); // Current year for academic_year in enrollments

            List<AttendanceRecord> attendanceRecords = new ArrayList<>();
            for (int i = 0; i < recordsArray.size(); i++) {
                JsonObject recordObj = recordsArray.get(i).getAsJsonObject();
                int studentId = recordObj.get("studentId").getAsInt();
                String status = recordObj.get("status").getAsString(); // "P" or "A"

                AttendanceRecord record = new AttendanceRecord();
                record.setStudentId(studentId);
                record.setAttendanceStatus(status);

                attendanceRecords.add(record);
            }

            // --- CRITICAL CHANGE HERE: Convert Instant to LocalDate using ZoneOffset.UTC ---
            boolean recordsAdded = attendanceDAO.addMultipleAttendanceRecordsToAttendanceTable(
                                                attendanceRecords,
                                                courseIdForAttendanceRecords,
                                                currentSession.getSessionStartTime().atOffset(ZoneOffset.UTC).toLocalDate(), // FIX HERE
                                                academicYear,
                                                currentSession.getProgramId(),
                                                currentSession.getSessionId() 
                                            );
            // --- END CRITICAL CHANGE ---

            if (recordsAdded) {
                boolean sessionStatusUpdated = attendanceDAO.updateAttendanceSessionStatus(sessionIdFromRequest, "COMPLETED");

                if (sessionStatusUpdated) {
                    conn.commit();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Attendance submitted successfully!");
                    if (httpSession != null) {
                        httpSession.removeAttribute("currentAttendanceSession"); // Clear session attribute
                    }
                } else {
                    conn.rollback();
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Attendance records saved, but failed to update session status.");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Set status for partial success/failure
                }
            } else {
                conn.rollback();
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to save attendance records.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Set status for failure to save records
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Rollback is already handled by the try-with-resources if auto-commit was set to false.
            // If an exception occurs before conn.commit(), the transaction will be rolled back automatically
            // when the connection is returned to the pool (or closed if not from pool).
            // However, explicit rollback is good if you have multiple operations within the same transaction.
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Set status for DB errors
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing request: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Set status for general processing errors
        } finally {
            // No need for explicit conn.close() or conn.setAutoCommit(true) here
            // as try-with-resources handles connection closure/return to pool,
            // and commit/rollback logic is inside the try block.
            out.flush(); // Ensure all buffered output is sent
        }
        out.print(gson.toJson(jsonResponse));
    }
}
