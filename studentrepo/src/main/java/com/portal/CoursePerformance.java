package com.portal;

public class CoursePerformance {
    // --- Constants for Maximum Marks based on your rules ---
    private static final double MAX_CIE_MARKS = 50.0;
    private static final double MAX_SEE_MARKS = 80.0;

    private String courseCode;
    private String subjectName;
    private Double ia1Marks;
    private Double ia2Marks;
    private Double seeMarks;
    private Integer totalClassesHeld;
    private Integer classesAttended;

    public CoursePerformance(String courseCode, String subjectName,
                             Double ia1Marks, Double ia2Marks, Double seeMarks,
                             Integer totalClassesHeld, Integer classesAttended) {
        this.courseCode = courseCode;
        this.subjectName = subjectName;
        this.ia1Marks = ia1Marks;
        this.ia2Marks = ia2Marks;
        this.seeMarks = seeMarks;
        this.totalClassesHeld = totalClassesHeld;
        this.classesAttended = classesAttended;
    }

    // --- Raw Data Getters ---
    public String getCourseCode() { return courseCode; }
    public String getSubjectName() { return subjectName; }
    public Double getIa1Marks() { return ia1Marks; }
    public Double getIa2Marks() { return ia2Marks; }
    public Double getSeeMarks() { return seeMarks; }
    public Integer getTotalClassesHeld() { return totalClassesHeld; }
    public Integer getClassesAttended() { return classesAttended; }

    // --- Dynamic Calculation Getters for JSP ---
    public double getMaxCieMarks() { return MAX_CIE_MARKS; }
    public double getMaxSeeMarks() { return MAX_SEE_MARKS; }

    public Double getCie1Percentage() {
        if (ia1Marks == null) return null;
        return (ia1Marks / MAX_CIE_MARKS) * 100.0;
    }

    public Double getCie2Percentage() {
        if (ia2Marks == null) return null;
        return (ia2Marks / MAX_CIE_MARKS) * 100.0;
    }

    public Double getCombinedCieMarks() {
        if (ia1Marks == null || ia2Marks == null) return null;
        return (ia1Marks + ia2Marks) / 2.0;
    }

    private Double getCombinedCieForFinal() {
        if (ia1Marks == null || ia2Marks == null) return null;
        return (ia1Marks + ia2Marks) / 5.0;
    }

    public Double getFinalTotalMarks() {
        Double combinedCieFinal = getCombinedCieForFinal();
        if (combinedCieFinal == null || seeMarks == null) return null;
        return combinedCieFinal + seeMarks;
    }

    public Double getFinalMaxMarks() {
        return ((MAX_CIE_MARKS * 2) / 5.0) + MAX_SEE_MARKS;
    }

    public Double getFinalPercentage() {
        Double total = getFinalTotalMarks();
        Double max = getFinalMaxMarks();
        if (total == null || max == null || max == 0) return null;
        return (total / max) * 100.0;
    }
    
    public Double getAttendancePercentage() {
        if (totalClassesHeld != null && classesAttended != null && totalClassesHeld > 0) {
            return ((double) classesAttended / totalClassesHeld) * 100.0;
        }
        return 0.0;
    }
}