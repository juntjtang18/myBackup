package com.myBackup.server.restapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myBackup.server.repository.BackupRepositoryService;
import com.myBackup.server.repository.BackupRepository;
import com.myBackup.server.repository.BackupRepositoryBuilder;

import java.io.File; // Import File for directory checking
import java.util.List;

@RestController
@RequestMapping("/api/repositories")
public class RepositoriesRestController {
    private static final Logger logger = LoggerFactory.getLogger(RepositoriesRestController.class);

    private final BackupRepositoryService backupReposService;
    
    public RepositoriesRestController(BackupRepositoryService backupReposService) {
        this.backupReposService = backupReposService;
    }
    
    @GetMapping("/list")
    public String getRepositories(@RequestParam("clientID") String clientID) {
        // For testing, return a simple string message including the clientID
        return "Received clientID: " + clientID;
    }
    
    @PostMapping("/list-by-clientID")
    public ResponseEntity<List<BackupRepository>> getAllRepositories(@RequestBody ClientIDRequest request) {
        logger.debug("/api/repositories/list called with {}.", request.getClientID());
        
        List<BackupRepository> repositories = backupReposService.getAllByClientID(request.getClientID());
        return new ResponseEntity<>(repositories, HttpStatus.OK);
    }
    
    @PostMapping("/create")
    public ResponseEntity<String> createRepository(@RequestBody CreateRepositoryRequest request) {
        logger.debug("/api/repositories/create called with destDirectory: {}, clientID: {}.", request.getDestDirectory(), request.getClientID());

        // Check if the destination directory exists
        File directory = new File(request.getDestDirectory());
        if (directory.exists() && directory.isDirectory()) {
            // If exists, return a confirmation message
            return new ResponseEntity<>("Directory exists. Please confirm to proceed.", HttpStatus.OK);
        }

        // If it doesn't exist, proceed to create the repository
        BackupRepository newRepository = new BackupRepositoryBuilder(backupReposService)
                                            .connectTo(request.getServerUrl(), request.getServerName())
                                            .mountTo(request.getDestDirectory())
                                            .grantTo(request.getClientID())
                                            .build();
        
        if (newRepository != null) {
            // create the repository in repository service
            backupReposService.createRepository(newRepository);
            return new ResponseEntity<>("Repository created successfully!", HttpStatus.CREATED); // Return created status
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Bad request if creation fails
        }
    }
    
    @PostMapping("/create/confirm")
    public ResponseEntity<BackupRepository> confirmCreateRepository(@RequestBody CreateRepositoryRequest request) {
        logger.debug("/api/repositories/create/confirm called with destDirectory: {}, clientID: {}.", request.getDestDirectory(), request.getClientID());

        BackupRepository newRepository = new BackupRepositoryBuilder(backupReposService)
                                            .connectTo(request.getServerUrl(), request.getServerName())
                                            .mountTo(request.getDestDirectory())
                                            .grantTo(request.getClientID())
                                            .build();

        if (newRepository != null) {
            backupReposService.createRepository(newRepository);
            return new ResponseEntity<>(newRepository, HttpStatus.CREATED); // Return created status
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Bad request if creation fails
        }
    }
}
