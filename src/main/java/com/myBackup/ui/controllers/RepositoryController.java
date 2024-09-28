package com.myBackup.ui.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.myBackup.client.services.UUIDService;
import com.myBackup.server.meta.ServersService;
import com.myBackup.server.meta.ServersService.Server;
import com.myBackup.server.repository.BackupRepositoryService;

@Controller
public class RepositoryController {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);
    
    private final UUIDService uuidService;
    private final BackupRepositoryService backupRepositoryService;
    private final ServersService serversService;
    
    public RepositoryController(UUIDService uuidService, BackupRepositoryService backupRepositoryService, ServersService serversService) {
    	this.uuidService = uuidService;
    	this.backupRepositoryService = backupRepositoryService;
    	this.serversService = serversService;
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
}
