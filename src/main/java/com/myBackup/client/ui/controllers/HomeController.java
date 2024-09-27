package com.myBackup.client.ui.controllers;

import com.myBackup.client.Utility;
import com.myBackup.client.services.UUIDService;
import com.myBackup.models.ClientIDRequest;
import com.myBackup.models.UserDto;
import com.myBackup.server.repository.BackupRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    // Create a logger instance
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UUIDService uuidService;
    
    @Autowired
    private RestTemplate restTemplate;

    public HomeController(UUIDService uuidService) {
    	this.uuidService = uuidService;
    }
    
    @GetMapping("/login")
    public String showLoginForm(Model model, Principal principal) {
        // Get the system's MAC address and the user's name
        String username = System.getProperty("user.name");
        String password = Utility.getMACAddress(); // Or your MAC address fetching logic

        // Pass data to the view
        UserDto user = new UserDto(username, password, "", "");
        model.addAttribute("user", user);

        return "login"; // Returns to login.html
    }

    
    @GetMapping(value= {"/", "/home"})
    public String home(Model model) {
    	
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        logger.debug("authentication {}", authentication);
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Log user details
            logger.info("Current user: username={}, role={}", userDetails.getUsername(), userDetails.getAuthorities());

            model.addAttribute("user", userDetails);
            model.addAttribute("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet()));

            //String currentUsername = authentication.getName(); // Get the username from Authentication
            
            return "home"; // Return the view name for the home page
        }

        // If no user is authenticated, redirect to login page
        return "redirect:/login";
    }

    @GetMapping("/start")
    public String start(Model model) {
    	logger.debug("/start triggered.");
    	
    	String clientID = uuidService.getUUID();
        model.addAttribute("clientID", clientID);

        // Create the request body with clientID
        ClientIDRequest clientIDRequest = new ClientIDRequest(clientID);

        // Call the REST API to check if any accessible repositories exist for this client
        String apiUrl = "http://localhost:8080/api/repositories/list";

        // Log the API request URL
        logger.info("Making request to API: {}", apiUrl);

        // Use ParameterizedTypeReference to handle generic type List<BackupRepository>
        ResponseEntity<List<BackupRepository>> response = restTemplate.exchange(
                apiUrl,
                org.springframework.http.HttpMethod.POST, // Change to POST
                new HttpEntity<>(clientIDRequest), // Set request body
                new ParameterizedTypeReference<List<BackupRepository>>() {}
        );

        // Log the status of the response
        logger.info("API response status: {}", response.getStatusCode());

        // Log the body of the response
        List<BackupRepository> repositories = response.getBody();
        if (repositories != null) {
            logger.info("Repositories found: {}", repositories.size());
            repositories.forEach(repo -> logger.info("Repository: {}", repo.toString()));
        } else {
            logger.info("No repositories found or response body is null.");
        }

        // Check if the response body is empty (no repositories found)
        if (repositories == null || repositories.isEmpty()) {
            // Add clientID to the model and return backup-repo view
            model.addAttribute("clientID", clientID);
            return "backup-repo"; // Return to the backup-repo view
        }

        // If repositories exist, proceed to the main application page
        return "redirect:/dashboard";
    }

}
