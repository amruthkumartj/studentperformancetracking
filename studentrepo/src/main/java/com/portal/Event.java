package com.portal;

// UPDATED: Changed imports for direct database compatibility
import java.sql.Date;
import java.sql.Time;

public class Event {
    private int eventId;
    private int facultyId;
    private String eventCategory;
    private String eventName;
    private String eventDescription;
    private String eventLink;
    private Date eventDate; // UPDATED: Changed from LocalDate
    private Time eventTime; // UPDATED: Changed from LocalTime
    private Date registrationEndDate; // NEW: Added field
    private String facultyName;

    // Constructors
    public Event() {}

    // You can use a constructor like this for creating new events
    public Event(int facultyId, String eventCategory, String eventName, String eventDescription, String eventLink, Date eventDate, Time eventTime, Date registrationEndDate) {
        this.facultyId = facultyId;
        this.eventCategory = eventCategory;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventLink = eventLink;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.registrationEndDate = registrationEndDate;
    }


    // Getters and Setters for all fields
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventLink() {
        return eventLink;
    }

    public void setEventLink(String eventLink) {
        this.eventLink = eventLink;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Time getEventTime() {
        return eventTime;
    }

    public void setEventTime(Time eventTime) {
        this.eventTime = eventTime;
    }
    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    // NEW: Getter and Setter for the new field
    public Date getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(Date registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }
}