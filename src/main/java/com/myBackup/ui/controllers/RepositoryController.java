package com.myBackup.ui.controllers;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.myBackup.client.services.UUIDService;
import com.myBackup.models.Job;
import com.myBackup.security.User;
import com.myBackup.server.meta.ServersService;
import com.myBackup.server.meta.ServersService.Server;
import com.myBackup.services.JobService;
import com.myBackup.services.bfs.Repository;
import com.myBackup.services.bfs.RepositoryStorage;

@Controller
public class RepositoryController {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);
    
    @Autowired
    private UUIDService uuidService;
    @Autowired
    private RepositoryStorage backupRepositoryService;
    @Autowired
    private ServersService serversService;
    @Autowired
    private JobService jobService;
        
    @GetMapping("/repository-create")
    public String createRepository(Model model) {
        // Retrieve the list of servers
        List<Server> servers = serversService.getServers(); 
        model.addAttribute("servers", servers); // Add the servers to the model

        String clientID = uuidService.getUUID(); // Use the UUIDService to get the client ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                model.addAttribute("username", username);
            } else {
                model.addAttribute("username", principal.toString());
            }
        }
        
        model.addAttribute("clientID", clientID); 
    	return "repository-create"; 
    }
    
    @GetMapping("/repository")
    public String getRepositoryDetails(@RequestParam("id") String repoId, Model model) {
        logger.info("Fetching details for repository ID: {}", repoId);
        
        // Fetch the repository details using the repoId
        Repository repository = backupRepositoryService.getRepositoryById(repoId); // Implement this method in your service
        
        if (repository == null) {
            logger.error("Repository not found for ID: {}", repoId);
            model.addAttribute("error", "Repository not found");
            return "error"; // Return to an error page or handle as needed
        }
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
        
        model.addAttribute("repository", repository); // Add repository details to the model        
        model.addAttribute("clientID", uuidService.getUUID()); // Add the clientID to the model
        List<Job.JobType> jobTypes = Arrays.asList(Job.JobType.values());
        model.addAttribute("jobTypes", jobTypes);
        List<Job> jobs = jobService.getByRepositoryID(repoId);
        model.addAttribute("jobs", jobs);
        
        return "repository-details"; // The view to render the repository details
    }    
}
