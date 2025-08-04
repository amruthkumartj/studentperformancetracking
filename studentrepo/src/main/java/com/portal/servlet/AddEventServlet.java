package com.portal.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.portal.EventsDAO;
import com.portal.FacultyDAO; // Make sure your FacultyDAO is imported
import com.portal.Event;
import com.portal.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/add-event-servlet")
public class AddEventServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson;

    // This constructor sets up Gson to handle SQL Date/Time types correctly
    // without needing new files.
    public AddEventServlet() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
            @Override
            public void write(JsonWriter out, Date value) throws IOException {
                if (value == null) out.nullValue(); else out.value(value.toString());
            }
            @Override
            public Date read(JsonReader in) throws IOException {
                if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
                String dateStr = in.nextString();
                if (dateStr.isEmpty()) return null;
                return Date.valueOf(dateStr);
            }
        });
        gsonBuilder.registerTypeAdapter(Time.class, new TypeAdapter<Time>() {
            @Override
            public void write(JsonWriter out, Time value) throws IOException {
                if (value == null) out.nullValue(); else out.value(value.toString());
            }
            @Override
            public Time read(JsonReader in) throws IOException {
                if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
                String timeStr = in.nextString();
                if (timeStr.isEmpty()) return null;
                if (timeStr.length() == 5) timeStr += ":00";
                return Time.valueOf(timeStr);
            }
        });
        this.gson = gsonBuilder.create();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	 request.setCharacterEncoding("UTF-8");

         response.setContentType("application/json; charset=UTF-8");
         response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> jsonResponse = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "User not logged in.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        
        try (Reader reader = request.getReader()) {
            
            // === THIS IS THE FINAL LOGIC ===
            
            // 1. Get the user_id from the session.
            int userId = currentUser.getId();
            
            // 2. Use your FacultyDAO to look up the corresponding faculty_id.
            FacultyDAO facultyDAO = new FacultyDAO();
            int facultyId = facultyDAO.getFacultyIdByUserId(userId);

            // 3. Check if a valid faculty_id was found.
            // We check for <= 0 to handle the -1 your DAO returns.
            if (facultyId <= 0) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Could not find a faculty profile for the logged-in user. Event cannot be saved.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // 4. Proceed with creating the event, using the correct facultyId.
            Event event = gson.fromJson(reader, Event.class);
            event.setFacultyId(facultyId); // Set the CORRECT faculty ID

            EventsDAO eventsDAO = new EventsDAO();
            eventsDAO.addEvent(event);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Event hosted successfully!");
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}