// src/main/java/com/portal/Student.java
package com.portal;

// REMOVE 'extends User' - Student no longer inherits from User
// public class Student extends User { // Assuming Student extends User for ID and possibly username
public class Student implements java.io.Serializable { // Implement Serializable as good practice for DTOs

    private int studentId;
    private int userId; // NEW: Foreign key to link to the User table
    private String fullName;
    private int programId;
    private String programName; // To display program name directly
    private int semester;
    private String phone;
    private String email; // Keep this field if student-specific email is needed, otherwise remove if User.email is sufficient

    // Constructors
    public Student() {
        // No super() call needed as it no longer extends User
    }

    // Updated constructor to include userId
    public Student(int studentId, int userId, String fullName, int programId, int semester, String phone, String email) {
        this.studentId = studentId;
        this.userId = userId; // Initialize userId
        this.fullName = fullName;
        this.programId = programId;
        this.semester = semester;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters for Student-specific properties
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // NEW: Getter and Setter for userId
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Student{" +
               "studentId=" + studentId +
               ", userId=" + userId + // Include userId in toString
               ", fullName='" + fullName + '\'' +
               ", programId=" + programId +
               ", programName='" + programName + '\'' +
               ", semester=" + semester +
               ", phone='" + phone + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}