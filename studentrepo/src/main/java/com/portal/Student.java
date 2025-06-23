package com.portal;

public class Student {
    private int studentId;
    private String fullName;
    private String course;
    private int semester;
    private String phone;
    private String email;

    // Getters and Setters for all fields
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
    public String getCourse() {
        return course;
    }
    public void setCourse(String course) {
        this.course = course;
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
}