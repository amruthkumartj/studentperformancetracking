package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.portal.AttendanceDAO;
import com.portal.AttendanceRecord;
import com.portal.AttendanceSession;
import com.portal.DBUtil;

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
import java.time.LocalDate;     // Keep this import
// import java.time.LocalDateTime; // REMOVE THIS IMPORT if not used elsewhere
import java.time.Instant;       // ADD THIS IMPORT
import java.time.ZoneOffset;    // ADD THIS IMPORT for UTC conversion
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