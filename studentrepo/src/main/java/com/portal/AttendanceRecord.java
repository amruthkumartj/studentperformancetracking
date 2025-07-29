// src/main/java/com/portal/AttendanceRecord.java
package com.portal;

import java.time.LocalDateTime;
import java.sql.Date; // Import java.sql.Date for the attendanceDate field

public class AttendanceRecord {
    private int recordId;
    private String sessionId;
    private int studentId;
    private String attendanceStatus; // "P" or "A"
    private LocalDateTime markingTime; // Represents the exact timestamp of marking
    private int enrollmentId;

    // --- NEW FIELDS ADDED FOR DISPLAY ---
    private String studentName; // To display student's name in the table
    private Date attendanceDate; // To display the date of attendance (often derived from markingTime or a dedicated column)
    private String subjectName; // To display the subject name
    // --- END NEW FIELDS ---

    // Constructor
    public AttendanceRecord() {}

    // Getters and Setters (existing ones)
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public LocalDateTime getMarkingTime() {
        return markingTime;
    }

    public void setMarkingTime(LocalDateTime markingTime) {
        this.markingTime = markingTime;
    }

    // --- Getters and Setters for NEW FIELDS ---
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getSubjectName() {
        return subjectName;
    }
    



   
    public int getEnrollmentId() {
        return enrollmentId;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    // --- END Getters and Setters for NEW FIELDS ---

    // Optional: toString for debugging
    @Override
    public String toString() {
        return "AttendanceRecord{" +
               "recordId=" + recordId +
               ", sessionId='" + sessionId + '\'' +
               ", studentId=" + studentId +
               ", studentName='" + studentName + '\'' +
               ", attendanceStatus='" + attendanceStatus + '\'' +
               ", markingTime=" + markingTime +
               ", attendanceDate=" + attendanceDate +
               ", subjectName='" + subjectName + '\'' +
               '}';
    }
}