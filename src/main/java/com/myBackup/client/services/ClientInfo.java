package com.myBackup.client.services;

import com.myBackup.security.TokenInfo;

public class ClientInfo {
    private String uuid;
    private TokenInfo tokenInfo; // TokenInfo object to hold access and refresh tokens
    private String username; // Add username
    private String email; // Add email

    public ClientInfo(String uuid, String username, String email) {
        this.uuid = uuid;
        this.tokenInfo = new TokenInfo(); // Generate TokenInfo
        this.username = username; // Initialize username
        this.email = email; // Initialize email
    }

    public String getUuid() {
        return uuid;
    }

    public TokenInfo getTokenInfo() {
        return tokenInfo; // Getter for tokenInfo
    }

    public void setTokenInfo(TokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo; // Setter for tokenInfo
    }

    public String getUsername() {
        return username; // Getter for username
    }

    public String getEmail() {
        return email; // Getter for email
    }

    public String toJson() {
        // Convert the ClientInfo object to JSON format (you can use libraries like Jackson)
        return String.format(
            "{\"uuid\":\"%s\",\"tokenInfo\":%s,\"username\":\"%s\",\"email\":\"%s\"}",
            uuid, tokenInfo.toString(), username, email);
    }
}
