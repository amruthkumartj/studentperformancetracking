package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.portal.Event;
import com.portal.EventsDAO;
import com.portal.FacultyDAO; // <-- IMPORT FACULTY DAO
import com.portal.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList; // <-- IMPORT ARRAYLIST
import java.util.Collections;
import java.util.List;

@WebServlet("/fetch-faculty-events")
public class FetchFacultyEventsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson;

    // Constructor to set up Gson with our custom date/time handlers
    public FetchFacultyEventsServlet() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
            @Override public void write(JsonWriter out, Date value) throws IOException { if (value == null) out.nullValue(); else out.value(value.toString()); }
            @Override public Date read(JsonReader in) throws IOException { /* Not needed for this servlet */ return null; }
        });
        gsonBuilder.registerTypeAdapter(Time.class, new TypeAdapter<Time>() {
            @Override public void write(JsonWriter out, Time value) throws IOException { if (value == null) out.nullValue(); else out.value(value.toString()); }
            @Override public Time read(JsonReader in) throws IOException { /* Not needed for this servlet */ return null; }
        });
        this.gson = gsonBuilder.create();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(gson.toJson(Collections.singletonMap("error", "User not authenticated.")));
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        EventsDAO eventsDAO = new EventsDAO();
        List<Event> events = new ArrayList<>(); // Initialize as empty list

        try {
            // === THIS IS THE CORRECTED ADMIN/FACULTY LOGIC ===
            
            if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                // If user is an Admin, get all events from all faculty
                events = eventsDAO.getAllEventsForAdmin();
            } else {
                // If user is a regular Faculty, we must look up their specific faculty_id
                
                // 1. Get user_id from session
                int userId = currentUser.getId();
                
                // 2. Use FacultyDAO to find the correct faculty_id
                FacultyDAO facultyDAO = new FacultyDAO();
                int facultyId = facultyDAO.getFacultyIdByUserId(userId);
                
                // 3. Fetch events ONLY if a valid faculty_id was found
                if (facultyId > 0) {
                    events = eventsDAO.getEventsByFaculty(facultyId);
                }
                // If no faculty_id is found, the 'events' list remains empty, which is correct.
            }

            String jsonResponse = gson.toJson(events);
            response.getWriter().write(jsonResponse);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Collections.singletonMap("error", "Database error occurred.")));
        }
    }
}