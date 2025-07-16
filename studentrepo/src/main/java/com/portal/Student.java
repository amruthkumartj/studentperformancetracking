// src/main/java/com/portal/Student.java
package com.portal;

public class Student extends User { // Assuming Student extends User for ID and possibly username

    private int studentId;
    private String fullName;
    private int programId;
    private String programName; // To display program name directly
    private int semester;
    private String phone; // <--- ADD THIS FIELD
    private String email; // <--- ADD THIS FIELD

    // Constructors
    public Student() {
        super(); // Call the default constructor of the superclass (User)
    }

    public Student(int studentId, String fullName, int programId, int semester, String phone, String email) {
        // You might set the userId/username from parent if student IS a user
        // For simplicity, assuming studentId maps to userId if extending User.
        super(studentId, null, "STUDENT", true); // Example: assuming student is always approved, and username might be null or set later
        this.studentId = studentId;
        this.fullName = fullName;
        this.programId = programId;
        this.semester = semester;
        this.phone = phone; // Initialize phone
        this.email = email; // Initialize email
    }

    // Getters and Setters for Student-specific properties
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
        super.setId(studentId); // Assuming studentId is also the user_id if Student extends User
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

    // <--- ADD THESE GETTER AND SETTER METHODS FOR PHONE
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // <--- ADD THESE GETTER AND SETTER METHODS FOR EMAIL
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // You may also want to override toString() for easy debugging
    @Override
    public String toString() {
        return "Student{" +
               "studentId=" + studentId +
               ", fullName='" + fullName + '\'' +
               ", programId=" + programId +
               ", programName='" + programName + '\'' +
               ", semester=" + semester +
               ", phone='" + phone + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}