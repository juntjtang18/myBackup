package com.myBackup.ui.controllers;

import com.myBackup.client.Utility;
import com.myBackup.client.services.UUIDService;
import com.myBackup.models.UserDto;
import com.myBackup.server.meta.ServersService;
import com.myBackup.server.meta.ServersService.Server;
import com.myBackup.server.repository.BackupRepositoryService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private UUIDService uuidService;
    private final BackupRepositoryService backupRepositoryService;
    private ServersService serversService; // Inject the ServersService to access server data

    public HomeController(UUIDService uuidService, BackupRepositoryService backupRepositoryService, ServersService serversService) {
        this.uuidService = uuidService;
        this.backupRepositoryService = backupRepositoryService;
        this.serversService = serversService;
    }

    @GetMapping("/")
    public String originalStart(HttpServletRequest request, Model model) {
        // Get the remote IP address of the request
        String remoteAddr = request.getRemoteAddr();
        
        logger.info("Request from {} received. ", remoteAddr);
        
        // Check if the request is coming from the local machine
        if (isLocalRequest(remoteAddr)) {
            // Create a new UserDto object and set username and MAC address
            UserDto user = new UserDto();
            user.setUsername(System.getProperty("user.name"));
            user.setPassword(Utility.getMACAddress());  // Assuming password field will be used for MAC address

            model.addAttribute("user", user);  // Add the user object to the model

            logger.info("Request from local machine, redirecting to auto-login.html with user: {}", user);

            // Redirect to auto-login.html
            return "auto-login";  // Assuming this template uses the 'user' object for binding
        } else {
            logger.info("Request not from local machine, redirecting to login.html.");

            // Redirect to login.html
            return "login";  // This template should handle user input separately
        }
    }

    // Utility method to check if the request is local
    private boolean isLocalRequest(String remoteAddr) {
        return "127.0.0.1".equals(remoteAddr) || "localhost".equals(remoteAddr) || "::1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr);
    }
    
    @GetMapping("/home")
    public String home() {
        return "home";
    }
    
    @GetMapping("/start")
    public String start() {
        int repositoryCount = backupRepositoryService.repositoryCount();
        logger.info("Repository count: {}", repositoryCount);
        
        if (repositoryCount == 0) {
            logger.info("No repositories found, redirecting to repository-create.");
            return "redirect:/repository-create";  // Redirect to the add repository endpoint
        } else {
            logger.info("Repositories found, redirecting to dashboard.");
            return "redirect:/dashboard";  // Redirect to the dashboard endpoint
        }
    }
}
