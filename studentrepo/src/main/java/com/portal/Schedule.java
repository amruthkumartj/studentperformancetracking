// src/main/java/com/portal/Schedule.java
package com.portal;

import java.time.LocalDate;
import java.time.LocalTime;

public class Schedule {
    private int scheduleId;
    private int facultyId;
    private int programId;
    private int semester;
    private String courseId;
    private String subjectName;
    private LocalDate classDate;
    private LocalTime classTime;
    private int durationMinutes;
    private String location;
    private boolean isRecurring;
    private LocalDate recurrenceEndDate;

    // Constructors
    public Schedule() {}

    public Schedule(int facultyId, int programId, int semester, String courseId, String subjectName,
                    LocalDate classDate, LocalTime classTime, int durationMinutes, String location,
                    boolean isRecurring, LocalDate recurrenceEndDate) {
        this.facultyId = facultyId;
        this.programId = programId;
        this.semester = semester;
        this.courseId = courseId;
        this.subjectName = subjectName;
        this.classDate = classDate;
        this.classTime = classTime;
        this.durationMinutes = durationMinutes;
        this.location = location;
        this.isRecurring = isRecurring;
        this.recurrenceEndDate = recurrenceEndDate;
    }

    // Getters and Setters
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public LocalDate getClassDate() { return classDate; }
    public void setClassDate(LocalDate classDate) { this.classDate = classDate; }

    public LocalTime getClassTime() { return classTime; }
    public void setClassTime(LocalTime classTime) { this.classTime = classTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean isRecurring) { this.isRecurring = isRecurring; }

    public LocalDate getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }

    @Override
    public String toString() {
        return "Schedule{" +
               "scheduleId=" + scheduleId +
               ", facultyId=" + facultyId +
               ", programId=" + programId +
               ", semester=" + semester +
               ", courseId='" + courseId + '\'' +
               ", subjectName='" + subjectName + '\'' +
               ", classDate=" + classDate +
               ", classTime=" + classTime +
               ", durationMinutes=" + durationMinutes +
               ", location='" + location + '\'' +
               ", isRecurring=" + isRecurring +
               ", recurrenceEndDate=" + recurrenceEndDate +
               '}';
    }
}