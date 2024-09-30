package com.myBackup.ui.controllers;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.myBackup.client.services.UUIDService;
import com.myBackup.models.Job;
import com.myBackup.server.meta.ServersService;
import com.myBackup.server.meta.ServersService.Server;
import com.myBackup.server.repository.Repository;
import com.myBackup.server.repository.RepositoryService;
import com.myBackup.services.JobService;

@Controller
public class RepositoryController {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);
    
    private final UUIDService uuidService;
    private final RepositoryService backupRepositoryService;
    private final ServersService serversService;
    private final JobService jobService;
    
    public RepositoryController(UUIDService uuidService, RepositoryService backupRepositoryService, ServersService serversService, JobService jobService) {
    	this.uuidService = uuidService;
    	this.backupRepositoryService = backupRepositoryService;
    	this.serversService = serversService;
    	this.jobService = jobService;
    }
    
    @GetMapping("/repository-create")
    public String createRepository(Model model) {
        // Retrieve the list of servers
        List<Server> servers = serversService.getServers(); 
        model.addAttribute("servers", servers); // Add the servers to the model

        // Get the clientID from UUIDService
        String clientID = uuidService.getUUID(); // Use the UUIDService to get the client ID
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("user", authentication);

        model.addAttribute("clientID", clientID); // Add the client ID to the model
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
        
        model.addAttribute("repository", repository); // Add repository details to the model
        
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("user", authentication.getPrincipal());
        // Get the clientID from UUIDService
        String clientID = uuidService.getUUID();
        model.addAttribute("clientID", clientID); // Add the clientID to the model
        List<Job.JobType> jobTypes = Arrays.asList(Job.JobType.values());
        model.addAttribute("jobTypes", jobTypes);
        List<Job> jobs = jobService.getByRepositoryID(repoId);
        model.addAttribute("jobs", jobs);
        
        return "repository-details"; // The view to render the repository details
    }    
}
