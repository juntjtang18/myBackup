package com.myBackup.services;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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
            String clientId = getMacAddress(); // Send appropriate client information for registration
            String response = restTemplate.postForObject(LOCAL_SERVER_REGISTER_URL, clientId, String.class);
            
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

    public static String getMacAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();
            byte[] mac = network.getHardwareAddress();

            if (mac != null) {
                StringBuilder macAddress = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                return macAddress.toString();
            }
        }
        return null;
    }
}
