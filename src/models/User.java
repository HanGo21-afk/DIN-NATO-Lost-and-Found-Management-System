package models;

import java.time.LocalDateTime;


public class User {
    private int id;
    private String fullName;
    private String username;
    private String passwordHash;
    private String role;
    private LocalDateTime createdAt;

    public User(int id, String fullName, String username, String passwordHash,
                String role, LocalDateTime createdAt) {
        this.id = id;
        setFullName(fullName);
        setUsername(username);
        this.passwordHash = passwordHash;
        setRole(role);
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Full name is required.");
        this.fullName = fullName.trim();
    }

    public void setUsername(String username) {
        if (username == null || username.trim().length() < 3)
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        this.username = username.trim();
    }

    public void setRole(String role) {
        if (!"Admin".equals(role) && !"Staff".equals(role))
            throw new IllegalArgumentException("Role must be Admin or Staff.");
        this.role = role;
    }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
