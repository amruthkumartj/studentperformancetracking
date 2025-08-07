package com.portal.studservlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.portal.AttendanceRecord;
import com.portal.AttendanceSession;
import com.portal.DBUtil;
import com.portal.datatransfer_access.AttendanceDAO;

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
import java.time.ZoneOffset;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/SubmitAttendanceServlet")
public class SubmitAttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();
    private AttendanceDAO attendanceDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        attendanceDAO = new AttendanceDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        HttpSession session = request.getSession(false);

        Connection conn = null; // A single connection for the entire transaction

        try {
            // Step 1: Get a connection and start the transaction
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // Step 2: Parse the request body
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);
            
            int sessionIdFromRequest = requestData.get("sessionId").getAsInt();
            JsonArray recordsArray = requestData.getAsJsonArray("records");

            // Step 3: Validate the session
            AttendanceSession currentSession = (AttendanceSession) session.getAttribute("currentAttendanceSession");
            if (currentSession == null || currentSession.getSessionId() != sessionIdFromRequest) {
                throw new ServletException("Invalid or expired attendance session.");
            }

            // Step 4: Prepare the list of records
            List<AttendanceRecord> attendanceRecords = new ArrayList<>();
            for (int i = 0; i < recordsArray.size(); i++) {
                JsonObject recordObj = recordsArray.get(i).getAsJsonObject();
                AttendanceRecord record = new AttendanceRecord();
                record.setStudentId(recordObj.get("studentId").getAsInt());
                record.setAttendanceStatus(recordObj.get("status").getAsString());
                attendanceRecords.add(record);
            }

            // Step 5: Call the DAO method, PASSING THE CONNECTION
            boolean recordsAdded = attendanceDAO.addMultipleAttendanceRecordsToAttendanceTable(
                conn, // <-- This is the crucial change
                attendanceRecords,
                currentSession.getCourseId(),
                currentSession.getSessionStartTime().atOffset(ZoneOffset.UTC).toLocalDate(),
                Year.now().getValue(),
                currentSession.getProgramId(),
                currentSession.getSessionId()
            );

            // Step 6: Finalize the transaction
         // ...
            if (recordsAdded) {
                // Pass the existing connection to the update method
                attendanceDAO.updateAttendanceSessionStatus(conn, sessionIdFromRequest, "COMPLETED");
                conn.commit(); // Commit the entire transaction
                // ...
            
            // ...
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Attendance submitted successfully!");
                if (session != null) {
                    session.removeAttribute("currentAttendanceSession");
                }
            } else {
                throw new SQLException("Failed to save one or more attendance records.");
            }

        } catch (Exception e) {
            // If any error occurs, roll back the entire transaction
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace(); // Log rollback failure
                }
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always ensure the connection is closed
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}