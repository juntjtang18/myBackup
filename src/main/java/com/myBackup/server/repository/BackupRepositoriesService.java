package com.myBackup.server.repository;

import jakarta.annotation.PreDestroy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBackup.models.BackupJob;

@Service
public class BackupRepositoriesService {

    private static final Logger logger = LoggerFactory.getLogger(BackupRepositoriesService.class);
    private static final String REPOSITORY_FILE_PATH = Paths.get(System.getProperty("user.dir"), "config", "server", "repositories.json").toString();
    private final Map<String, BackupRepository> repositoryCache = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final ObjectMapper objectMapper; // Injected ObjectMapper

    public BackupRepositoriesService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadRepositories(); // Load repositories at the start
    }

    // Save repositories cache to the JSON file
    private void saveRepositories() {
        lock.lock();
        try {
            File file = new File(REPOSITORY_FILE_PATH);
            File directory = file.getParentFile(); // Get the directory of the repository file

            // Check if the directory exists, if not, create it
            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs(); // Create the directory
                if (dirCreated) {
                    logger.info("Created directories: {}", directory.getAbsolutePath());
                } else {
                    logger.error("Failed to create directories: {}", directory.getAbsolutePath());
                    return; // Exit if directory creation fails
                }
            }

            // Save the repository cache to the JSON file using injected ObjectMapper
            objectMapper.writeValue(file, repositoryCache.values());
            logger.info("Saved {} repositories to file.", repositoryCache.size());

        } catch (IOException e) {
            logger.error("Error saving repositories to file: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Load repositories from the file
    private void loadRepositories() {
        lock.lock();
        logger.info("Load repositories from file {}", REPOSITORY_FILE_PATH);
        try {
            File file = new File(REPOSITORY_FILE_PATH);
            if (file.exists()) {
                BackupRepository[] loadedRepositories = objectMapper.readValue(file, BackupRepository[].class);
                for (BackupRepository repository : loadedRepositories) {
                    repositoryCache.put(repository.getRepoID(), repository);
                }
                logger.info("Loaded {} repositories from file.", repositoryCache.size());
            } else {
                logger.warn("Repository file does not exist: {}", REPOSITORY_FILE_PATH);
            }
        } catch (IOException e) {
            logger.error("Error loading repositories from file: {}", e.getMessage());
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
    
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up BackupRepositoriesService, saving repositories...");
        saveRepositories(); // Persist the repositories before the service is destroyed
    }

    // Method to create a new BackupRepository instance
    public BackupRepository buildRepository() {
        return new BackupRepository();
    }
    
	public ObjectMapper getObjectMapper() {
		// TODO Auto-generated method stub
		return objectMapper;
	}
}
