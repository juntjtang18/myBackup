package com.myBackup.services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Service;

@Service
public class HashManager {

    public static String generateUniqueHash(String input) {
    	return generateHash(input);
    }

    private static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            
            // Use StringBuilder to store the hex representation
            StringBuilder hashString = new StringBuilder();
            
            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
                if (hashString.length() >= 12) {
                    return hashString.toString().substring(0, 12); 
                }
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error generating hash", e);
        }
        return ""; // Default return in case of failure (should not reach here)
    }
    
}
