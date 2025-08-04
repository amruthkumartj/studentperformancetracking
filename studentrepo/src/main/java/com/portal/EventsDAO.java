package com.portal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// NEW: Assuming you create and use the DatabaseUtil class
import com.portal.DBUtil; 

public class EventsDAO {

    // UPDATED: This now uses your central DatabaseUtil
    private Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    // UPDATED: To correctly save the new 'registration_end_date' field
    public void addEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (faculty_id, event_category, event_name, event_description, event_link, event_date, event_time, registration_end_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, event.getFacultyId());
            ps.setString(2, event.getEventCategory());
            ps.setString(3, event.getEventName());
            ps.setString(4, event.getEventDescription());
            ps.setString(5, event.getEventLink());
            ps.setDate(6, event.getEventDate()); // No conversion needed now
            
            if (event.getEventTime() != null) {
                ps.setTime(7, event.getEventTime()); // No conversion needed now
            } else {
                ps.setNull(7, java.sql.Types.TIME);
            }

            // Handle the new optional date field
            if (event.getRegistrationEndDate() != null) {
                ps.setDate(8, event.getRegistrationEndDate());
            } else {
                ps.setNull(8, java.sql.Types.DATE);
            }

            ps.executeUpdate();
        }
    }
    
 // In com.portal.EventsDAO.java

 // NEW: Method for the Admin to get all events from all faculty
 public List<Event> getAllEventsForAdmin() throws SQLException {
     List<Event> events = new ArrayList<>();
     // This query joins the events and faculty tables to get the faculty's full name
     String sql = "SELECT e.*, f.full_name FROM events e JOIN faculty f ON e.faculty_id = f.faculty_id ORDER BY e.event_date DESC";
     
     try (Connection con = getConnection();
          PreparedStatement ps = con.prepareStatement(sql);
          ResultSet rs = ps.executeQuery()) {
         
         while (rs.next()) {
             Event event = new Event();
             event.setEventId(rs.getInt("event_id"));
             event.setFacultyId(rs.getInt("faculty_id"));
             event.setEventCategory(rs.getString("event_category"));
             event.setEventName(rs.getString("event_name"));
             event.setEventDescription(rs.getString("event_description"));
             event.setEventLink(rs.getString("event_link"));
             event.setEventDate(rs.getDate("event_date"));
             event.setEventTime(rs.getTime("event_time"));
             event.setRegistrationEndDate(rs.getDate("registration_end_date"));
             
             // Set the faculty name from the joined table
             event.setFacultyName(rs.getString("full_name")); 
             
             events.add(event);
         }
     }
     return events;
 }
 
 
 

    // NEW: Method to get all events for a specific faculty member (for the dashboard)
    public List<Event> getEventsByFaculty(int facultyId) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE faculty_id = ? ORDER BY event_date DESC, event_time DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, facultyId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getInt("event_id"));
                event.setFacultyId(rs.getInt("faculty_id"));
                event.setEventCategory(rs.getString("event_category"));
                event.setEventName(rs.getString("event_name"));
                event.setEventDescription(rs.getString("event_description"));
                event.setEventLink(rs.getString("event_link"));
                event.setEventDate(rs.getDate("event_date")); // No conversion needed
                event.setEventTime(rs.getTime("event_time")); // No conversion needed
                event.setRegistrationEndDate(rs.getDate("registration_end_date")); // Get the new field
                events.add(event);
            }
        }
        return events;
    }
    
    // NEW: The method for our automatic cleanup scheduler
    public void deleteExpiredEvents() throws SQLException {
        // This query deletes events where the registration end date is in the past.
        // CURDATE() gets today's date in MySQL.
        String sql = "DELETE FROM events WHERE registration_end_date IS NOT NULL AND registration_end_date < CURDATE()";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Event Cleanup Task: Successfully deleted " + rowsAffected + " expired event(s).");
            } else {
                // This is normal, it will print most of the time when there's nothing to clean up.
                System.out.println("Event Cleanup Task: No expired events to delete.");
            }
        }
    }

    // UPDATED: Your existing method, now aligned with the new Event model
    public List<Event> getUpcomingEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE event_date >= CURDATE() ORDER BY event_date ASC, event_time ASC LIMIT 5";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getInt("event_id"));
                event.setFacultyId(rs.getInt("faculty_id"));
                event.setEventCategory(rs.getString("event_category"));
                event.setEventName(rs.getString("event_name"));
                event.setEventDescription(rs.getString("event_description"));
                event.setEventLink(rs.getString("event_link"));
                event.setEventDate(rs.getDate("event_date")); // No conversion needed
                event.setEventTime(rs.getTime("event_time")); // No conversion needed
                event.setRegistrationEndDate(rs.getDate("registration_end_date")); // Get the new field
                events.add(event);
            }
        }
        return events;
    }
}