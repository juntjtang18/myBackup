package com.myBackup.ui.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.myBackup.client.Utility;
import com.myBackup.client.services.UUIDService;

@Controller
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    @Autowired
    private UUIDService uuidService;
    
    @GetMapping("/dashboard")
    public String createRepository(Model model ) {
        // Add the authenticated user to the model (full Authentication object)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("user", authentication.getPrincipal());
        // Get the clientID from UUIDService
        String clientID = uuidService.getUUID();
        model.addAttribute("clientID", clientID); // Add the clientID to the model
        
        logger.info("return dashboard.html and pass in 'user'{} and 'clientID'{}. ", authentication.getPrincipal(), clientID);
        
        return "dashboard"; 
    }
}
