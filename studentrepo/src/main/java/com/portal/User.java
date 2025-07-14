package com.portal;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private String role;
    private boolean isApproved; // ADD THIS FIELD

    public User() {}

    public User(int id, String username, String role, boolean isApproved) { // Update constructor
        this.id = id;
        this.username = username;
        this.role = role;
        this.isApproved = isApproved; // Set the new field
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // ADD GETTER AND SETTER FOR isApproved
    public boolean isApproved() { return isApproved; } // Standard getter for boolean is 'isFieldName()'
    public void setApproved(boolean approved) { this.isApproved = approved; }
}