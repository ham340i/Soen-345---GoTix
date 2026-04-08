package com.example.gotix.model;

public class User {
    private String id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String phone;
    private long createdAt;

    // Original constructor for existing service logic
    public User(String id, String email, String phone, long createdAt) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // New constructor for Login/Register logic
    public User(String id, String username, String password, String role, long createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public long getCreatedAt() { return createdAt; }
}
