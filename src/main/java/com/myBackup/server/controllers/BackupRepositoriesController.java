package com.myBackup.server.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myBackup.client.ui.controllers.HomeController;
import com.myBackup.models.ClientIDRequest;
import com.myBackup.models.CreateRepositoryRequest;
import com.myBackup.server.repository.BackupRepositoriesService;
import com.myBackup.server.repository.BackupRepository;
import com.myBackup.server.repository.BackupRepositoryBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
public class BackupRepositoriesController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final BackupRepositoriesService backupReposService;
    
    public BackupRepositoriesController(BackupRepositoriesService backupReposService) {
        this.backupReposService = backupReposService;
    }

    @PostMapping("/list")
    public ResponseEntity<List<BackupRepository>> getAllRepositories(@RequestBody ClientIDRequest request) {
    	logger.debug("/api/repositories/list called with {}.", request.getClientID());
    	
        List<BackupRepository> repositories = backupReposService.getAllByClientID(request.getClientID());
        return new ResponseEntity<>(repositories, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<BackupRepository> createRepository(@RequestBody CreateRepositoryRequest request) {
        logger.debug("/api/repositories/create called with destDirectory: {}, clientID: {}.", request.getDestDirectory(), request.getClientID());

        // Use the service to create a new repository
        BackupRepository newRepository = new BackupRepositoryBuilder(backupReposService)
        										.mountTo(request.getDestDirectory())
        										.grantTo(request.getClientID())
        										.build();
        
        if (newRepository != null) {
            return new ResponseEntity<>(newRepository, HttpStatus.CREATED); // Return created status
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Bad request if creation fails
        }
    }
    
}
