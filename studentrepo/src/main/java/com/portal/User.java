package com.portal;

import java.io.Serializable;
// import java.time.LocalDateTime; // Uncomment if you intend to map the 'created_at' timestamp column

public class User implements Serializable {
    private int id; // Maps to user_id in the database
    private String username;
    private String passwordHash; // Maps to pwd_hash in the database
    private String role;
    private String email;        // Maps to email in the database
    private boolean isApproved;  // Maps to is_approved in the database
    // private LocalDateTime createdAt; // Maps to created_at in the database (uncomment if needed)

    public User() {
        // Default constructor
    }

    /**
     * Parameterized constructor for User DTO.
     * @param id The user's ID (primary key).
     * @param username The user's username.
     * @param passwordHash The hashed password of the user.
     * @param role The role of the user (e.g., "STUDENT", "FACULTY", "ADMIN").
     * @param email The email address of the user.
     * @param isApproved The approval status of the user (true if approved, false otherwise).
     */
    public User(int id, String username, String passwordHash, String role, String email, boolean isApproved) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.isApproved = isApproved;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() { // Getter for email
        return email;
    }

    public boolean isApproved() { // Getter for isApproved (standard for boolean fields)
        return isApproved;
    }

    // public LocalDateTime getCreatedAt() { // Getter for createdAt (uncomment if needed)
    //     return createdAt;
    // }


    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) { // Setter for email
        this.email = email;
    }

    public void setApproved(boolean approved) { // Setter for isApproved
        this.isApproved = approved;
    }

    // public void setCreatedAt(LocalDateTime createdAt) { // Setter for createdAt (uncomment if needed)
    //     this.createdAt = createdAt;
    // }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", role='" + role + '\'' +
               ", email='" + email + '\'' +
               ", isApproved=" + isApproved +
               '}';
    }
}