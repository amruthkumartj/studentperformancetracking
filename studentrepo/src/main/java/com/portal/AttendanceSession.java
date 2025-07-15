package com.portal;

// Import Instant (and potentially ZonedDateTime if you need to work with specific timezones for input/output)
import java.time.Instant;
// Keep these for now if other parts of your app still use them, but try to migrate
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime; // You will remove this for sessionStartTime/sessionExpiryTime


public class AttendanceSession {
    private int sessionId;
    private String courseId;
    private String topic;
    private int facultyId;
    // --- CHANGE THESE FIELDS ---
    private Instant sessionStartTime; // Change from LocalDateTime to Instant
    private Instant sessionExpiryTime; // Change from LocalDateTime to Instant
    // --- END CHANGE ---
    private String status;
    private String location; // Add location based on DB schema

    // --- NEW FIELDS TO SUPPORT JSP DISPLAY ---
    private int programId;
    private String subjectName; // Maps to course_name from 'courses' table
    private int semester;
    private String programName; // To display on JSP (from programs table via students)
    // --- END NEW FIELDS ---


    // Constructor (for creating new sessions - without sessionId initially)
    public AttendanceSession(String courseId, String topic, int facultyId,
                             Instant sessionStartTime, Instant sessionExpiryTime, // Change parameter types to Instant
                             String status, String location) {
        this.courseId = courseId;
        this.topic = topic;
        this.facultyId = facultyId;
        this.sessionStartTime = sessionStartTime;
        this.sessionExpiryTime = sessionExpiryTime;
        this.status = status;
        this.location = location;
    }

    // Default constructor for retrieval (from DB)
    public AttendanceSession() {
    }

    // --- Getters and Setters for existing fields ---
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    // --- CHANGE THESE GETTERS AND SETTERS ---
    public Instant getSessionStartTime() { return sessionStartTime; } // Change return type
    public void setSessionStartTime(Instant sessionStartTime) { this.sessionStartTime = sessionStartTime; } // Change parameter type

    public Instant getSessionExpiryTime() { return sessionExpiryTime; } // Change return type
    public void setSessionExpiryTime(Instant sessionExpiryTime) { this.sessionExpiryTime = sessionExpiryTime; } // Change parameter type
    // --- END CHANGE ---

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // --- Getters and Setters for NEW FIELDS ---
    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
}