// src/main/java/com/portal/StudentListItem.java
package com.portal;

public class StudentListItem {
    private String studentId; // Assuming student_id can be treated as String for display
    private String studentName;
    private String programName; // To display program name directly in the list
    private int semester;

    public StudentListItem() {
        // Default constructor
    }

    public StudentListItem(String studentId, String studentName, String programName, int semester) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.programName = programName;
        this.semester = semester;
    }

    // Getters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    @Override
    public String toString() {
        return "StudentListItem{" +
               "studentId='" + studentId + '\'' +
               ", studentName='" + studentName + '\'' +
               ", programName='" + programName + '\'' +
               ", semester=" + semester +
               '}';
    }
}
