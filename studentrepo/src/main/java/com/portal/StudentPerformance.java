// src/main/java/com/portal/StudentPerformance.java
package com.portal;

import java.util.List;

public class StudentPerformance {

    private int studentId;
    private String studentName;
    private String programName; // <--- ADD THIS FIELD
    private int semester;       // <--- ADD THIS FIELD (if not already present for semester-specific performance)
    private List<CoursePerformance> coursePerformances;
    private String overallAnalysis; // e.g., "Excellent", "Needs Improvement"
    private double overallAttendancePercentage;

    // Default constructor
    public StudentPerformance() {
    }

    // Constructor with fields (you can add more as needed)
    public StudentPerformance(int studentId, String studentName, String programName, int semester, List<CoursePerformance> coursePerformances, String overallAnalysis) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.programName = programName; // Initialize in constructor
        this.semester = semester;       // Initialize in constructor
        this.coursePerformances = coursePerformances;
        this.overallAnalysis = overallAnalysis;
    }

    // Getters and Setters

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    // <--- ADD THESE GETTER AND SETTER METHODS FOR PROGRAM NAME
    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    // <--- ADD THESE GETTER AND SETTER METHODS FOR SEMESTER (if not already present)
    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public List<CoursePerformance> getCoursePerformances() {
        return coursePerformances;
    }

    public void setCoursePerformances(List<CoursePerformance> coursePerformances) {
        this.coursePerformances = coursePerformances;
    }

    public String getOverallAnalysis() {
        return overallAnalysis;
    }

    public void setOverallAnalysis(String overallAnalysis) {
        this.overallAnalysis = overallAnalysis;
    }
    public double getOverallAttendancePercentage() {
        return overallAttendancePercentage;
    }

    public void setOverallAttendancePercentage(double overallAttendancePercentage) {
        this.overallAttendancePercentage = overallAttendancePercentage;
    }

    // You might also want to add a toString() method for easier debugging
    @Override
    public String toString() {
        return "StudentPerformance{" +
               "studentId=" + studentId +
               ", studentName='" + studentName + '\'' +
               ", programName='" + programName + '\'' +
               ", semester=" + semester +
               ", overallAttendancePercentage=" + overallAttendancePercentage + 
               ", coursePerformances=" + coursePerformances +
               ", overallAnalysis='" + overallAnalysis + '\'' +
               '}';
    }
}