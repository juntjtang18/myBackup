package com.myBackup.ui.controllers;

import com.myBackup.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/")
    public String home(Model model) {
        // Check if the user is already registered
        boolean isRegistered = registrationService.isUserRegistered();
        
        // If not registered, register the client to the local server
        if (!isRegistered) {
            boolean registrationSuccess = registrationService.registerClientToLocalServer();
            if (registrationSuccess) {
                model.addAttribute("message", "Client registered successfully to the local server.");
            } else {
                model.addAttribute("message", "Failed to register client to the local server.");
            }
        } else {
            model.addAttribute("message", "Welcome back! Client is already registered.");
        }

        return "home";
    }
}
