package com.myBackup.server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myBackup.server.repository.BackupRepository;
import com.myBackup.server.repository.BackupRepositoryManager;
import com.myBackup.server.repository.BackupRepositoryManagerImpl;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
public class BackupRepositoryController {

    private final BackupRepositoryManager backupRepositoryManager = new BackupRepositoryManagerImpl();

    @PostMapping("/create")
    public ResponseEntity<BackupRepository> createRepository(
            @RequestParam String destDir, 
            @RequestParam String clientID) {
        
        // Create the repository using the manager
        BackupRepository createdRepository = backupRepositoryManager.create(destDir, clientID);
        
        return ResponseEntity.status(201).body(createdRepository); // Return 201 Created with the repository
    }

    @GetMapping("/list")
    public ResponseEntity<List<BackupRepository>> getAllRepositories(@RequestParam String clientID) {
        List<BackupRepository> repositories = backupRepositoryManager.getAllByClientID(clientID);
        return ResponseEntity.ok(repositories); // Return 200 OK with the list of repositories
    }

    @GetMapping("/{repoID}")
    public ResponseEntity<BackupRepository> getRepository(@PathVariable String repoID) {
        // There should be a method in the manager to retrieve by ID if needed
        BackupRepository repository = backupRepositoryManager.getAll().stream()
            .filter(repo -> repo.getRepoID().equals(repoID))
            .findFirst()
            .orElse(null);
        
        if (repository != null) {
            return ResponseEntity.ok(repository); // Return 200 OK with the repository
        } else {
            return ResponseEntity.notFound().build(); // Return 404 if not found
        }
    }

    @PutMapping("/update/{repoID}")
    public ResponseEntity<BackupRepository> updateRepository(@PathVariable String repoID, 
                                                             @RequestBody BackupRepository updatedRepo) {
        // Assuming the updatedRepo has the correct data including repoID
        if (backupRepositoryManager.has(repoID)) {
            // You may need a specific update method or modify the existing repository
            BackupRepository existingRepo = backupRepositoryManager.getAll().stream()
                .filter(repo -> repo.getRepoID().equals(repoID))
                .findFirst()
                .orElse(null);
            if (existingRepo != null) {
                existingRepo.setDestDirectory(updatedRepo.getDestDirectory());
                return ResponseEntity.ok(existingRepo); // Return 200 OK with the updated repository
            }
        }
        return ResponseEntity.notFound().build(); // Return 404 if not found
    }

    @DeleteMapping("/delete/{repoID}")
    public ResponseEntity<Void> deleteRepository(@PathVariable String repoID) {
        if (backupRepositoryManager.has(repoID)) {
            backupRepositoryManager.delete(repoID);
            return ResponseEntity.ok().build(); // Return 200 OK if deletion is successful
        } else {
            return ResponseEntity.notFound().build(); // Return 404 if not found
        }
    }
}
