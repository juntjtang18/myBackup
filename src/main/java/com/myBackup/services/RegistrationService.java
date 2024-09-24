package com.myBackup.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RegistrationService {

    private boolean isRegistered = false;
    private static final String LOCAL_SERVER_REGISTER_URL = "http://localhost:8080/restapi/services/register";

    public boolean isUserRegistered() {
        return isRegistered;
    }

    public boolean registerClientToLocalServer() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String clientInfo = "client-info"; // Send appropriate client information for registration
            String response = restTemplate.postForObject(LOCAL_SERVER_REGISTER_URL, clientInfo, String.class);
            
            // Simulate a successful registration
            if (response != null && response.contains("success")) {
                isRegistered = true;
                System.out.println("Client registered to local server successfully.");
                return true;
            } else {
                System.err.println("Registration failed.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            return false;
        }
    }
}
