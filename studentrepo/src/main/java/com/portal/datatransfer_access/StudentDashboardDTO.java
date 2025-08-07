package com.portal.datatransfer_access;

import java.util.List;
import java.util.Map;

import com.portal.CoursePerformance;

/**
 * Main DTO to transfer all necessary data for the student dashboard to the JSP.
 */
public class StudentDashboardDTO {

    private int studentId;
    private String studentName;
    private int currentSemester;
    private String programName;
    private String Email;
    private int programId;
    
    // Key: Semester number (e.g., 1, 2). Value: List of course performance data for that semester.
    private Map<Integer, List<CoursePerformance>> performanceBySemester;
    
    // Overall attendance for the homepage widget
    private OverallAttendanceDTO overallAttendance;

    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
    
    public String getEmail() { return Email; }
    public void setEmail(String Email) { this.Email = Email; }

    public int getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(int currentSemester) { this.currentSemester = currentSemester; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public Map<Integer, List<CoursePerformance>> getPerformanceBySemester() { return performanceBySemester; }
    public void setPerformanceBySemester(Map<Integer, List<CoursePerformance>> performanceBySemester) { this.performanceBySemester = performanceBySemester; }
    
    public OverallAttendanceDTO getOverallAttendance() { return overallAttendance; }
    public void setOverallAttendance(OverallAttendanceDTO overallAttendance) { this.overallAttendance = overallAttendance; }
}