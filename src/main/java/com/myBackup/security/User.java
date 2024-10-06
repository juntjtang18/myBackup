package com.myBackup.security;

public class User {

    private String username;
    private String encryptedPassword;
    private String role;
    private String email;

    // Constructor
    public User(String username, String encryptedPassword, String role, String email) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.role = role;
        this.email = email;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
