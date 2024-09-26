package com.myBackup.server.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the BackupRepositoryManager interface.
 */
public class BackupRepositoryManagerImpl implements BackupRepositoryManager {

    // In-memory storage for repositories
    private Map<String, BackupRepository> repositories = new HashMap<>();

    @Override
    public BackupRepository create(String destDir, String clientID) {
        // Create a new repository
        BackupRepository repository = new BackupRepository(destDir, clientID);
        
        // Store it in the repositories map, using repoID as the key
        repositories.put(repository.getRepoID(), repository);
        
        // Persist changes to the repositories.json file
        persist();
        
        return repository;
    }

    @Override
    public BackupRepository create(BackupRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("BackupRepository cannot be null.");
        }
        // Store the provided repository in the repositories map
        repositories.put(repo.getRepoID(), repo);
        
        // Persist changes to the repositories.json file
        persist();
        
        return repo;
    }

    @Override
    public List<BackupRepository> getAllByClientID(String clientID) {
        // Filter repositories by clientID
        List<BackupRepository> clientRepos = new ArrayList<>();
        for (BackupRepository repo : repositories.values()) {
            if (repo.getClientIDs().contains(clientID)) {
                clientRepos.add(repo);
            }
        }
        return clientRepos;
    }

    @Override
    public List<BackupRepository> getAll() {
        // Return all repositories
        return new ArrayList<>(repositories.values());
    }

    @Override
    public boolean delete(String repoID) {
        // Remove the repository from the map if it exists
        boolean removed = repositories.remove(repoID) != null; // Returns true if removed, false if not found
        
        if (removed) {
            // Persist changes to the repositories.json file
            persist();
        }
        
        return removed;
    }

    @Override
    public boolean persist() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");
        // Define the file path for repositories.json
        File file = new File(currentDir, "repositories.json");

        try {
            // Write the repositories to the JSON file, overwriting if it exists
            Collection<BackupRepository> repoCollection = repositories.values();
            objectMapper.writeValue(file, repoCollection);
            return true; // Return true if successful
        } catch (IOException e) {
            e.printStackTrace(); // Log the error (consider using a logger instead)
            return false; // Return false if an error occurs
        }
    }

    @Override
    public boolean has(String repoID) {
        return repositories.containsKey(repoID);
    }
}
