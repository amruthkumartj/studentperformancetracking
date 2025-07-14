package com.portal; // This should be the same package as your Course.java

public class Program {
    private int programId;
    private String programName;

    // Default constructor
    public Program() {
    }

    // Constructor with fields
    public Program(int programId, String programName) {
        this.programId = programId;
        this.programName = programName;
    }

    // Getter for programId
    public int getProgramId() {
        return programId;
    }

    // Setter for programId
    public void setProgramId(int programId) {
        this.programId = programId;
    }

    // Getter for programName
    public String getProgramName() {
        return programName;
    }

    // Setter for programName
    public void setProgramName(String programName) {
        this.programName = programName;
    }

    @Override
    public String toString() {
        return "Program{" +
               "programId=" + programId +
               ", programName='" + programName + '\'' +
               '}';
    }
}