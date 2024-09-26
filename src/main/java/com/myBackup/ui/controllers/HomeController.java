package com.myBackup.ui.controllers;

import com.myBackup.services.RegistrationService;
import com.myBackup.client.Utility;
import com.myBackup.server.repository.BackupRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private Utility utility;

    @Autowired
    private RestTemplate restTemplate;

    // Create a logger instance
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String home(Model model) {
        
        return "home";
    }

    @GetMapping("/start")
    public String start(Model model) {
        // Get the MAC address of the client
        String macAddress = utility.getMACAddress();
        model.addAttribute("clientID", macAddress);

        // Call the REST API to check if any accessible repositories exist for this client
        String apiUrl = "http://localhost:8080/api/repositories/list?clientID=" + macAddress;

        // Log the API request URL
        logger.info("Making request to API: {}", apiUrl);

        // Use ParameterizedTypeReference to handle generic type List<BackupRepository>
        ResponseEntity<List<BackupRepository>> response = restTemplate.exchange(
                apiUrl,
                org.springframework.http.HttpMethod.GET,
                null,
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
            model.addAttribute("clientID", macAddress);
            return "backup-repo"; // Return to the backup-repo view
        }

        // If repositories exist, proceed to the main application page
        return "redirect:/dashboard";
    }
}
