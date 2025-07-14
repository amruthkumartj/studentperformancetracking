package com.portal; // Assuming this is the correct package

public class StudentMarkEntryDTO {
    private int studentId;
    private String fullName;
    private int enrollmentId; // Crucial for linking marks to student-course
    private Double marksObtained; // Renamed from 'marks' to match client-side and database
    private int facultyId;        // Added facultyId
    private String examType;      // Re-added examType to the DTO
    private String courseId;      // Added courseId to the DTO

    // Default constructor (important for Gson deserialization if no custom constructor is used for that purpose)
    public StudentMarkEntryDTO() {
    }

    // Constructor (optional, but good for explicit creation)
    public StudentMarkEntryDTO(int studentId, String fullName, int enrollmentId, Double marksObtained, int facultyId, String examType, String courseId) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.enrollmentId = enrollmentId;
        this.marksObtained = marksObtained;
        this.facultyId = facultyId;
        this.examType = examType;
        this.courseId = courseId;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
    }

    public String getExamType() { // Getter for examType
        return examType;
    }

    public void setExamType(String examType) { // Setter for examType
        this.examType = examType;
    }

    public String getCourseId() { // New getter for courseId
        return courseId;
    }

    public void setCourseId(String courseId) { // New setter for courseId
        this.courseId = courseId;
    }
}
