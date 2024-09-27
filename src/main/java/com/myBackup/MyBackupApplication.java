package com.myBackup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.myBackup.client.Utility;
import com.myBackup.security.UserService;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class MyBackupApplication {
    private final UserService userService;

    @Autowired
    public MyBackupApplication(UserService userService) {
        this.userService = userService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyBackupApplication.class, args);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyListener() {
        return event -> openBrowser("http://localhost:8080");
    }

    private void openBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                System.err.println("Failed to open the browser: " + e.getMessage());
            }
        } else {
            System.err.println("Desktop is not supported. Unable to open the browser.");
        }
    }
    
    @PostConstruct
    public void init() {
        String username = System.getProperty("user.name"); // Get the current user's name
        String macAddress = Utility.getMACAddress(); // Get the MAC address

        try {
            if (userService.isFirstUser()) { // Check if this is the first user
                userService.registerUser(username, macAddress, username + "@mybackup.com"); // Register the first user
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to check or register user", e);
        }
    }

}
