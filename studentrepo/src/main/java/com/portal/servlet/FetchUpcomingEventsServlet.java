package com.portal.servlet;

import com.google.gson.*;
import com.portal.Event;
import com.portal.datatransfer_access.EventsDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/FetchUpcomingEventsServlet")
public class FetchUpcomingEventsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Define the custom TypeAdapters for LocalDate and LocalTime as anonymous inner classes
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                @Override
                public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            })
            .registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
                @Override
                public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME));
                }
            })
            .create();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json; charset=UTF-8"); 
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            EventsDAO eventsDAO = new EventsDAO();
            List<Event> upcomingEvents = eventsDAO.getUpcomingEvents();

            if (upcomingEvents != null && !upcomingEvents.isEmpty()) {
                String jsonResponse = gson.toJson(upcomingEvents);
                out.print(jsonResponse);
            } else {
                out.print("[]");
            }

        } catch (Exception e) {
            // This is a temporary change to get the specific error message
            e.printStackTrace(); 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server Error: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}