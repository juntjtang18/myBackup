package com.myBackup.server.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBackup.server.repository.BackupRepository;
import com.myBackup.models.BackupJob;
import com.myBackup.server.repository.BackupRepositoriesService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class BackupRepositoryBuilder {
    private final BackupRepositoriesService service;
    private BackupRepository repository;

    public BackupRepositoryBuilder(BackupRepositoriesService service) {
        this.service = service;
        this.repository = service.buildRepository(); // Create a new repository instance
    }

    public BackupRepositoryBuilder mountTo(String destination) {
        // Validate the destination parameter
        if (destination == null || destination.isEmpty()) {
            throw new IllegalArgumentException("Destination directory must not be empty.");
        }

        Path dirPath = Paths.get(destination);

        // Check if the destination directory exists
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath); // Create the directory if it doesn't exist
                System.out.println("Destination directory created: " + destination);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create destination directory: " + destination, e);
            }
        } else {
            System.out.println("Using existing destination directory: " + destination);
            // Load jobs and client IDs from the existing directory
            loadJobs();
            loadClientIDs();
        }

        // Set the destination directory in the repository
        repository.setDestDirectory(destination);
        return this;
    }

    public BackupRepositoryBuilder grantTo(String clientID) {
        // Validate clientID
        if (clientID == null || clientID.isEmpty()) {
            throw new IllegalArgumentException("Client ID must not be empty.");
        }

        // Register the client ID
        repository.getClientIDs().add(clientID);

        // Persist the updated client IDs
        persistClientIDs();

        return this;
    }

    private void loadJobs() {
        Path jobsFilePath = Paths.get(repository.getDestDirectory(), "jobs.json");
        if (Files.exists(jobsFilePath)) {
            try {
                Map<String, BackupJob> loadedJobs = service.getObjectMapper().readValue(
                    jobsFilePath.toFile(),
                    service.getObjectMapper().getTypeFactory().constructMapType(Map.class, String.class, BackupJob.class)
                );
                repository.getJobs().putAll(loadedJobs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadClientIDs() {
        Path clientIDsFilePath = Paths.get(repository.getDestDirectory(), "clients.json");
        if (Files.exists(clientIDsFilePath)) {
            try {
                Set<String> loadedClientIDs = service.getObjectMapper().readValue(
                    clientIDsFilePath.toFile(),
                    service.getObjectMapper().getTypeFactory().constructCollectionType(Set.class, String.class)
                );
                repository.getClientIDs().addAll(loadedClientIDs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void persistClientIDs() {
        Path clientIDsFilePath = Paths.get(repository.getDestDirectory(), "clients.json");
        try {
            service.getObjectMapper().writeValue(clientIDsFilePath.toFile(), repository.getClientIDs());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to persist client IDs to " + clientIDsFilePath, e);
        }
    }

    public BackupRepository build() {
        return repository; // Return the fully constructed repository
    }
}
