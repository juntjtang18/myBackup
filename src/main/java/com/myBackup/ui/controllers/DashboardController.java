package com.myBackup.ui.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.myBackup.client.services.UUIDService;

@Controller
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    @Autowired
    private UUIDService uuidService;
    
    @GetMapping("/dashboard")
    public String createRepository(Model model ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = ""; 
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
                model.addAttribute("username", username);
            } else {
                model.addAttribute("username", principal.toString());
            }
        }
        String clientID = uuidService.getUUID();
        model.addAttribute("clientID", clientID); // Add the clientID to the model
        
        logger.info("return dashboard.html and pass in 'user'{} and 'clientID'{}. ", username, clientID);
        
        return "dashboard"; 
    }
}
