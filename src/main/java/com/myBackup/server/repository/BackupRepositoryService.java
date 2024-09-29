package com.myBackup.server.repository;

import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBackup.config.Config;

@Service
public class BackupRepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(BackupRepositoryService.class);
    private final String REPOSITORY_FILE_PATH;
    private final Map<String, BackupRepository> repositoryCache = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final ObjectMapper objectMapper; // Injected ObjectMapper
    private Config config;
    
    public BackupRepositoryService(ObjectMapper objectMapper, Config config) {
        this.objectMapper = objectMapper;
        this.config = config;
        this.REPOSITORY_FILE_PATH = this.config.getBackupRepositoryFilePath();
        logger.info("Repository file path set to: {}", REPOSITORY_FILE_PATH);
        
        loadRepositories(); // Load repositories at the start
        logger.info("Repositories loaded from file: {}", REPOSITORY_FILE_PATH);
    }

    // Method to get a repository by its ID
    public BackupRepository getRepositoryById(String repoId) {
        lock.lock();
        try {
            BackupRepository repository = repositoryCache.get(repoId); // Retrieve from cache
            if (repository == null) {
                logger.warn("Repository not found for ID: {}", repoId);
            } else {
                logger.info("Retrieved repository: {}", repository);
            }
            return repository; // Return the repository or null if not found
        } finally {
            lock.unlock();
        }
    }

 // Save repositories cache to the JSON file
    private void saveRepositories() {
        lock.lock();
        try {
            File file = new File(REPOSITORY_FILE_PATH);
            File directory = file.getParentFile(); // Get the directory of the repository file

            // Check if the directory exists; if not, create it
            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs(); // Create the directory
                if (dirCreated) {
                    logger.info("Created directories: {}", directory.getAbsolutePath());
                } else {
                    logger.error("Failed to create directories: {}", directory.getAbsolutePath());
                    return; // Exit if directory creation fails
                }
            }

            // Write all repositories as a JSON array
            try (FileWriter fileWriter = new FileWriter(file)) {
                objectMapper.writeValue(fileWriter, repositoryCache.values());
            }

            logger.info("Saved {} repositories to file as an array.", repositoryCache.size());

        } catch (IOException e) {
            logger.error("Error saving repositories to file: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Load repositories from the file
    private void loadRepositories() {
        lock.lock();
        logger.info("Loading repositories from file {}", REPOSITORY_FILE_PATH);
        try {
            File file = new File(REPOSITORY_FILE_PATH);
            if (file.exists()) {
                // Check if the file is not empty
                if (file.length() == 0) {
                    logger.warn("Repository file is empty: {}", REPOSITORY_FILE_PATH);
                    return; // Exit if the file is empty
                }

                // Load repositories from the file
                BackupRepository[] loadedRepositories = objectMapper.readValue(file, BackupRepository[].class);
                for (BackupRepository repository : loadedRepositories) {
                    repositoryCache.put(repository.getRepoID(), repository);
                }
                logger.info("Loaded {} repositories from file.", loadedRepositories.length); // Log number of loaded repositories
            } else {
                logger.warn("Repository file does not exist: {}", REPOSITORY_FILE_PATH);
            }
        } catch (IOException e) {
            logger.error("Error loading repositories from file: {} - {}", REPOSITORY_FILE_PATH, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Method to get all repositories by clientID
    public List<BackupRepository> getAllByClientID(String clientID) {
        lock.lock();
        try {
            List<BackupRepository> result = new ArrayList<>();
            for (BackupRepository repository : repositoryCache.values()) {
                if (repository.getClientIDs().contains(clientID)) {
                    result.add(repository);
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
    
    // Method to create a new BackupRepository instance
    public BackupRepository buildRepository() {
        return new BackupRepository();
    }

    // Method to create and add a new BackupRepository to the cache
    public void createRepository(BackupRepository repository) {
        lock.lock();
        try {
            // Check if the repository already exists
            if (repositoryCache.containsKey(repository.getRepoID())) {
                throw new IllegalArgumentException("Repository with ID " + repository.getRepoID() + " already exists.");
            }

            // Add the repository to the cache
            repositoryCache.put(repository.getRepoID(), repository);
            logger.info("Added repository with ID: {}", repository.getRepoID());

            // Persist the updated cache to the JSON file
            saveRepositories();
        } finally {
            lock.unlock();
        }
    }

    // Method to return the count of repositories
    public int repositoryCount() {
        lock.lock();
        try {
            return repositoryCache.size();
        } finally {
            lock.unlock();
        }
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up BackupRepositoriesService, saving repositories...");
        saveRepositories(); // Persist the repositories before the service is destroyed
    }
    
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
