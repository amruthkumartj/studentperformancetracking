package com.portal; // Ensure this matches your package structure

public class CoursePerformance {
    private String courseCode;
    private String subjectName;
    
    // Individual marks from DB
    private Double ia1Marks;
    private Double ia2Marks;
    private Double seeMarks;
    
    // Calculated marks
    private Double combinedCieMarks; // Average of IA1 + IA2
    private Double overallTotalMarks; // Combined CIE Avg + SEE
    private Double overallMaxMarks;   // Max possible for overallTotalMarks
    private Double overallPercentage; // overallTotalMarks / overallMaxMarks * 100

    // Attendance
    private Double attendancePercentage;
    private Integer totalClassesHeld;
    private Integer classesAttended;

    // Constructor (make sure to update all constructors if you have multiple)
    public CoursePerformance(String courseCode, String subjectName,
                             Double ia1Marks, Double ia2Marks, Double seeMarks,
                             Double combinedCieMarks,
                             Double overallTotalMarks, Double overallMaxMarks, Double overallPercentage,
                             Double attendancePercentage, Integer totalClassesHeld, Integer classesAttended) {
        this.courseCode = courseCode;
        this.subjectName = subjectName;
        this.ia1Marks = ia1Marks;
        this.ia2Marks = ia2Marks;
        this.seeMarks = seeMarks;
        this.combinedCieMarks = combinedCieMarks;
        this.overallTotalMarks = overallTotalMarks;
        this.overallMaxMarks = overallMaxMarks;
        this.overallPercentage = overallPercentage;
        this.attendancePercentage = attendancePercentage;
        this.totalClassesHeld = totalClassesHeld;
        this.classesAttended = classesAttended;
    }

    // --- Getters and Setters ---
    // Make sure all new fields have corresponding getters and setters for Gson to serialize them correctly

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Double getIa1Marks() { return ia1Marks; }
    public void setIa1Marks(Double ia1Marks) { this.ia1Marks = ia1Marks; }

    public Double getIa2Marks() { return ia2Marks; }
    public void setIa2Marks(Double ia2Marks) { this.ia2Marks = ia2Marks; }

    public Double getSeeMarks() { return seeMarks; }
    public void setSeeMarks(Double seeMarks) { this.seeMarks = seeMarks; }

    public Double getCombinedCieMarks() { return combinedCieMarks; }
    public void setCombinedCieMarks(Double combinedCieMarks) { this.combinedCieMarks = combinedCieMarks; }

    public Double getOverallTotalMarks() { return overallTotalMarks; }
    public void setOverallTotalMarks(Double overallTotalMarks) { this.overallTotalMarks = overallTotalMarks; }

    public Double getOverallMaxMarks() { return overallMaxMarks; }
    public void setOverallMaxMarks(Double overallMaxMarks) { this.overallMaxMarks = overallMaxMarks; }

    public Double getOverallPercentage() { return overallPercentage; }
    public void setOverallPercentage(Double overallPercentage) { this.overallPercentage = overallPercentage; }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public Integer getTotalClassesHeld() { return totalClassesHeld; }
    public void setTotalClassesHeld(Integer totalClassesHeld) { this.totalClassesHeld = totalClassesHeld; }

    public Integer getClassesAttended() { return classesAttended; }
    public void setClassesAttended(Integer classesAttended) { this.classesAttended = classesAttended; }
}