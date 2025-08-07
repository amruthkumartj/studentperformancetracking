// src/main/java/com/portal/ScheduleDAO.java
package com.portal.datatransfer_access;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.portal.DBUtil;
import com.portal.Schedule;

public class ScheduleDAO {

    /**
     * Adds a new daily/temporary class schedule to the database.
     * This method can also handle recurring schedules by inserting a series of records.
     *
     * @param facultyId The ID of the faculty member scheduling the class.
     * @param programId The ID of the program.
     * @param semester The semester number.
     * @param courseId The ID of the course.
     * @param subjectName The name of the subject.
     * @param classDate The date of the first class.
     * @param classTime The time of the class.
     * @param durationMinutes The duration of the class in minutes.
     * @param location The location of the class.
     * @param isRecurring True if the class should repeat.
     * @param recurrenceEndDate The end date for a recurring class, or null if not recurring.
     * @return true if the schedule(s) were added successfully, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean addDailySchedule(int facultyId, int programId, int semester, String courseId, String subjectName,
                                    LocalDate classDate, LocalTime classTime, int durationMinutes, String location,
                                    boolean isRecurring, LocalDate recurrenceEndDate) throws SQLException {
        String sql = "INSERT INTO dailyschedules (faculty_id, program_id, semester, course_id, subject_name, " +
                     "class_date, class_time, duration_minutes, location, is_recurring, recurrence_end_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Set parameters for the PreparedStatement
            ps.setInt(1, facultyId);
            ps.setInt(2, programId);
            ps.setInt(3, semester);
            ps.setString(4, courseId);
            ps.setString(5, subjectName);
            ps.setTime(7, Time.valueOf(classTime));
            ps.setInt(8, durationMinutes);
            ps.setString(9, location);
            ps.setBoolean(10, isRecurring);
            
            // Handle single or recurring classes
            LocalDate currentDate = classDate;
            do {
                ps.setDate(6, Date.valueOf(currentDate)); // Set the date for the current iteration
                if (isRecurring && recurrenceEndDate != null) {
                    ps.setDate(11, Date.valueOf(recurrenceEndDate));
                } else {
                    ps.setDate(11, null);
                }
                ps.addBatch(); // Add to batch for efficient bulk insert

                if (isRecurring) {
                    currentDate = currentDate.plusDays(1); // Move to the next day
                }
            } while (isRecurring && !currentDate.isAfter(recurrenceEndDate));
            
            int[] rowsAffected = ps.executeBatch();
            
            // Check if all rows in the batch were inserted successfully
            for (int rows : rowsAffected) {
                if (rows == 0) {
                    return false;
                }
            }
            return true;
            
        }
    }

    /**
     * Retrieves all daily schedules for a given program, semester, and date.
     *
     * @param programId The ID of the program.
     * @param semester The semester number.
     * @param date The specific date to retrieve schedules for.
     * @return A list of Schedule objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Schedule> getDailySchedules(int programId, int semester, LocalDate date) throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM dailyschedules WHERE program_id = ? AND semester = ? AND class_date = ? ORDER BY class_time ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, programId);
            ps.setInt(2, semester);
            ps.setDate(3, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapResultSetToSchedule(rs));
                }
            }
        }
        return schedules;
    }
    
 // REPLACE the old doesConflictExist method with this new one

    /**
     * Counts conflicts in the REGULAR timetable.
     * An overlap occurs if (StartA < EndB) and (StartB < EndA).
     * @return The number of conflicting regular classes.
     */
    public int countRegularConflicts(int programId, int semester, DayOfWeek day, LocalTime newStartTime, LocalTime newEndTime) throws SQLException {
        String sql = "SELECT COUNT(*) FROM regular_timetable " +
                     "WHERE program_id = ? AND semester = ? AND day_of_week = ? " +
                     "AND ? < end_time AND start_time < ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, programId);
            ps.setInt(2, semester);
            ps.setString(3, day.toString());
            ps.setTime(4, java.sql.Time.valueOf(newStartTime));
            ps.setTime(5, java.sql.Time.valueOf(newEndTime));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Counts conflicts in the EXTRA classes (dailyschedules) table for a specific date.
     * An overlap occurs if (StartA < EndB) and (StartB < EndA).
     * @return The number of conflicting extra classes.
     */

    public int countExtraConflicts(int programId, int semester, LocalDate classDate, LocalTime newStartTime, LocalTime newEndTime) throws SQLException {
        // This new query uses the more reliable INTERVAL syntax for time calculation.
        String sql = "SELECT COUNT(*) FROM dailyschedules " +
                     "WHERE program_id = ? AND semester = ? AND class_date = ? " +
                     "AND ? < (class_time + INTERVAL duration_minutes MINUTE) " + // Logic: new_start_time < existing_end_time
                     "AND class_time < ?";                                       // Logic: existing_start_time < new_end_time

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);
            ps.setInt(2, semester);
            ps.setDate(3, java.sql.Date.valueOf(classDate));
            ps.setTime(4, java.sql.Time.valueOf(newStartTime));
            ps.setTime(5, java.sql.Time.valueOf(newEndTime));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    public boolean addDailySchedulesForDates(int facultyId, int programId, int semester, String courseId, String subjectName,
            LocalTime classTime, int durationMinutes, String location,
            boolean isRecurring, LocalDate recurrenceEndDate, List<LocalDate> datesToSchedule) throws SQLException {

String sql = "INSERT INTO dailyschedules (faculty_id, program_id, semester, course_id, subject_name, " +
"class_date, class_time, duration_minutes, location, is_recurring, recurrence_end_date) " +
"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

try (Connection conn = DBUtil.getConnection();
PreparedStatement ps = conn.prepareStatement(sql)) {

// Loop through only the valid dates and add them to a batch
for (LocalDate validDate : datesToSchedule) {
ps.setInt(1, facultyId);
ps.setInt(2, programId);
ps.setInt(3, semester);
ps.setString(4, courseId);
ps.setString(5, subjectName);
ps.setDate(6, java.sql.Date.valueOf(validDate)); // Use the valid date from the list
ps.setTime(7, java.sql.Time.valueOf(classTime));
ps.setInt(8, durationMinutes);
ps.setString(9, location);
ps.setBoolean(10, isRecurring);
ps.setDate(11, recurrenceEndDate != null ? java.sql.Date.valueOf(recurrenceEndDate) : null);
ps.addBatch();
}

int[] rowsAffected = ps.executeBatch();
return rowsAffected.length > 0; // Return true if at least one row was inserted
}
}
    
    
    
    
 // Add this new method inside your ScheduleDAO.java class

    public List<Schedule> getWeeklyTimetable(int programId, int semester) throws SQLException {
        List<Schedule> weeklySchedules = new ArrayList<>();
        String sql = "SELECT rt.*, c.course_name FROM regular_timetable rt " +
                     "LEFT JOIN courses c ON rt.course_id = c.course_id " +
                     "WHERE rt.program_id = ? AND rt.semester = ? " +
                     "ORDER BY FIELD(rt.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), rt.start_time";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);
            ps.setInt(2, semester);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = new Schedule();
                    schedule.setSubjectName(rs.getString("course_name")); // Using course_name for display
                    schedule.setClassTime(rs.getTime("start_time").toLocalTime());
                    schedule.setLocation(rs.getString("location"));
                    // We need a way to store the day of the week. Let's reuse an existing field for simplicity.
                    schedule.setCourseId(rs.getString("day_of_week")); // Re-purposing courseId to hold the day string
                    weeklySchedules.add(schedule);
                }
            }
        }
        return weeklySchedules;
    }
    
    
    
    public List<Schedule> getRegularTimetable(int programId, int semester, DayOfWeek day) throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        // Note: We are reusing the 'Schedule' object for simplicity.
        // In a larger app, you might create a 'RegularTimetable' object.
        String sql = "SELECT rt.*, c.course_name FROM regular_timetable rt " +
                     "JOIN courses c ON rt.course_id = c.course_id " +
                     "WHERE rt.program_id = ? AND rt.semester = ? AND rt.day_of_week = ? " +
                     "ORDER BY rt.start_time ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);
            ps.setInt(2, semester);
            ps.setString(3, day.toString()); // e.g., "MONDAY"

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = new Schedule();
                    schedule.setScheduleId(rs.getInt("id"));
                    schedule.setProgramId(rs.getInt("program_id"));
                    schedule.setSemester(rs.getInt("semester"));
                    schedule.setCourseId(rs.getString("course_id"));
                    // We get the full course name from the JOIN
                    schedule.setSubjectName(rs.getString("course_name"));
                    schedule.setClassTime(rs.getTime("start_time").toLocalTime());
                    schedule.setLocation(rs.getString("location"));
                    // You can calculate duration if needed: end_time - start_time
                    schedules.add(schedule);
                }
            }
        }
        return schedules;
    }

    public List<Schedule> getAllExtraSchedules(int programId, int semester) throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM dailyschedules WHERE program_id = ? AND semester = ? " +
                     "ORDER BY class_date ASC, class_time ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, programId);
            ps.setInt(2, semester);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapResultSetToSchedule(rs));
                }
            }
        }
        return schedules;
    }
    
    /**
     * Deletes a specific schedule record by its schedule_id.
     *
     * @param scheduleId The ID of the schedule to delete.
     * @return true if the schedule was deleted, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean deleteSchedule(int scheduleId) throws SQLException {
        String sql = "DELETE FROM dailyschedules WHERE schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, scheduleId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Helper method to map a ResultSet to a Schedule DTO.
     */
    private Schedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(rs.getInt("schedule_id"));
        schedule.setFacultyId(rs.getInt("faculty_id"));
        schedule.setProgramId(rs.getInt("program_id"));
        schedule.setSemester(rs.getInt("semester"));
        schedule.setCourseId(rs.getString("course_id"));
        schedule.setSubjectName(rs.getString("subject_name"));
        schedule.setClassDate(rs.getDate("class_date").toLocalDate());
        schedule.setClassTime(rs.getTime("class_time").toLocalTime());
        schedule.setDurationMinutes(rs.getInt("duration_minutes"));
        schedule.setLocation(rs.getString("location"));
        schedule.setRecurring(rs.getBoolean("is_recurring"));
        Date recurrenceEndDate = rs.getDate("recurrence_end_date");
        if (recurrenceEndDate != null) {
            schedule.setRecurrenceEndDate(recurrenceEndDate.toLocalDate());
        }
        return schedule;
    }
}