package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.portal.AttendanceDAO;
import com.portal.AttendanceRecord;
import com.portal.AttendanceSession;
import com.portal.DBUtil;
// import com.portal.User; // Not used in this servlet, can remove import if not used elsewhere

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
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/SubmitAttendanceServlet")
public class SubmitAttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            AttendanceDAO attendanceDAO = new AttendanceDAO(conn);

            JsonObject requestData = gson.fromJson(request.getReader(), JsonObject.class);
            // CHANGED: Get sessionId as int, as per updated AttendanceSession and DB
            int sessionIdFromRequest = requestData.get("sessionId").getAsInt();
            JsonArray recordsArray = requestData.getAsJsonArray("records");

            HttpSession httpSession = request.getSession(false);
            AttendanceSession currentSession = null;
            if (httpSession != null) {
                // Ensure the object stored in session is of the NEW AttendanceSession type
                currentSession = (AttendanceSession) httpSession.getAttribute("currentAttendanceSession");
            }

            // Check if session is valid and matches
            // CHANGED: Compare int session IDs directly
            if (currentSession == null || currentSession.getSessionId() != sessionIdFromRequest) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid or expired attendance session. Session ID mismatch or not found in session.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // --- IMPORTANT: Removed old subjectName extraction logic ---
            // The corrected AttendanceSession now holds the actual 'course_id' (e.g., "MCA101")
            String courseIdForAttendanceRecords = currentSession.getCourseId(); // Use the actual course_id from session

            if (courseIdForAttendanceRecords == null || courseIdForAttendanceRecords.isEmpty()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Course ID not found in the current attendance session. Cannot submit attendance.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // --- DEBUGGING START ---
            System.out.println("--- SubmitAttendanceServlet Debugging ---");
            System.out.println("Session ID from Request: " + sessionIdFromRequest);
            System.out.println("Session ID from currentSession Object: " + currentSession.getSessionId());
            System.out.println("Course ID from Session (for attendance records): " + courseIdForAttendanceRecords);
            System.out.println("Session Start Time: " + currentSession.getSessionStartTime());
            // --- DEBUGGING END ---

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

         // In SubmitAttendanceServlet.java
         // Find the line where you call addMultipleAttendanceRecordsToAttendanceTable:
            boolean recordsAdded = attendanceDAO.addMultipleAttendanceRecordsToAttendanceTable(
                                            attendanceRecords,
                                            courseIdForAttendanceRecords,
                                            currentSession.getSessionStartTime().toLocalDate(),
                                            academicYear,
                                            currentSession.getProgramId(),
                                            currentSession.getSessionId() // <--- ADD THIS ARGUMENT
                                        );

            if (recordsAdded) {
                // CHANGED: Pass int sessionIdFromRequest to updateAttendanceSessionStatus
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
                }
            } else {
                conn.rollback();
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to save attendance records.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error processing request: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        out.print(gson.toJson(jsonResponse));
    }
}