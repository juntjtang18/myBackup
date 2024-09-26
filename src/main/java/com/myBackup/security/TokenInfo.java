package com.myBackup.security;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

// TokenInfo class to hold access and refresh tokens
public class TokenInfo implements Serializable {
    private static final long serialVersionUID = 1L; // Define a unique version ID
    public  static long ACCESS_TOKEN_VALID_TIME = 30 * 60;  // default the valid time is 30 minutes.   
    private String accessToken;
    private String refreshToken;
    private Instant accessTokenExpiresAt; // Timestamp of when the token expires
    private Instant refreshTokenExpiresAt;

    public TokenInfo() {
        this.accessToken = generateToken(); // Generate a secure random access token
        this.refreshToken = generateToken(); // Generate a secure random refresh token
        this.setAccessTokenExpiresAt(Instant.now().plusSeconds(30 * 60));  // 30 minutes later it will expire.
        this.setRefreshTokenExpiresAt(Instant.now().plusSeconds(365*24*3600));
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    private String generateToken() {
        // Generate a secure random token
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[24]; // 24 bytes = 192 bits, which can be Base64 encoded to a longer string
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes); // Encode to URL-safe Base64
    }

    public Instant getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public void setAccessTokenExpiresAt(Instant accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public Instant getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(Instant refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
    
    public void refreshAccessToken() {
        this.refreshToken = generateToken(); // Generate a secure random refresh token
        this.setAccessTokenExpiresAt(Instant.now().plusSeconds(30 * 60));  // 30 minutes later it will expire.    	
    }

    @Override
    public String toString() {
        return "TokenInfo{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", accessTokenExpiresAt=" + accessTokenExpiresAt +
                ", refreshTokenExpiresAt=" + refreshTokenExpiresAt +
                '}';
    }
}
