package com.myBackup.server.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.myBackup.models.BackupJob;

// Custom exception for directory issues
@SuppressWarnings("serial")
class DirectoryNotFoundException extends RuntimeException {
    public DirectoryNotFoundException(String message) {
        super(message);
    }
}

public class BackupRepository  {
    private String repoID;
    private String destDirectory; // Renamed variable
    private Map<String, BackupJob> jobs; // Map of jobs
    private Set<String> clientIDs;
    private ObjectMapper objectMapper; // Jackson ObjectMapper

    // Constructor
    public BackupRepository(String destDirectory, String clientID) {
        this.repoID = UUID.randomUUID().toString();
        this.destDirectory = destDirectory; // Updated variable usage
        this.jobs = new HashMap<>();
        this.clientIDs = new HashSet<>(); // Initialize the Set
        this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper
        
        // Verify the destination directory exists or create it
        Path dirPath = Paths.get(destDirectory);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath); // Create the directory if it doesn't exist
            } catch (IOException e) {
                throw new DirectoryNotFoundException("Failed to create destination directory: " + destDirectory);
            }
        } else if (!Files.isDirectory(dirPath)) {
            throw new DirectoryNotFoundException("Destination directory is not valid: " + destDirectory);
        }

        if (clientID != null && !clientID.isEmpty()) {
            this.clientIDs.add(clientID);
        }
        
        //init(); // Call init to create the files
        loadJobs(); // Load existing jobs from the JSON file
        loadClientIDs();
    }
    
    // Get the destination directory of the repository
    public String getDestDirectory() {
        return destDirectory;
    }

    // Set the destination directory of the repository
    public void setDestDirectory(String destDirectory) {
        this.destDirectory = destDirectory;
    }

    // Get all jobs (clients) associated with the repository
    public Map<String, BackupJob> getJobs() {
        return jobs;
    }

    // Add a new job (clientID) to the repository
    public void addJob(BackupJob job) {
        if (job != null) {
            jobs.put(job.getJobID(), job); // Assuming getJobID() returns a unique ID
            persist(); // Persist jobs and client IDs after adding a new one
        }
    }

    // Remove a job (clientID) from the repository
    public void removeJob(String jobID) {
        if (jobID != null) {
            jobs.remove(jobID); // Remove job from the map
            persist(); // Persist jobs and client IDs after removal
        }
    }

    // Get the repository ID
    public String getRepoID() {
        return repoID;
    }

    // Get the access list of client IDs
    public Set<String> getClientIDs() {
        return clientIDs;
    }

    // Method to persist jobs into a JSON file
    public void persistJobs() {
        Path jobsFilePath = Paths.get(destDirectory, "jobs.json"); // Path to jobs.json
        try {
            // Serialize the jobs map directly to JSON and write to file
            objectMapper.writeValue(jobsFilePath.toFile(), jobs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load jobs from the JSON file directly into a Map
    public void loadJobs() {
        Path jobsFilePath = Paths.get(destDirectory, "jobs.json"); // Path to jobs.json
        if (Files.exists(jobsFilePath)) {
            try {
                // Deserialize the jobs.json file back into a Map<String, BackupJob>
                jobs = objectMapper.readValue(
                    jobsFilePath.toFile(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, BackupJob.class)
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Public method to persist client IDs into a file
    public void persistClientIDs() {
        Path clientIDsFilePath = Paths.get(destDirectory, "clientIDs.txt"); // Path to clientIDs.txt
        StringBuilder clientData = new StringBuilder();
        for (String clientID : clientIDs) {
            clientData.append(clientID).append("\n");
        }
        try {
            Files.write(clientIDsFilePath, clientData.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // New method to load client IDs from the text file into the Set
    public void loadClientIDs() {
        Path clientIDsFilePath = Paths.get(destDirectory, "clientIDs.txt"); // Path to clientIDs.txt
        if (Files.exists(clientIDsFilePath)) {
            try (BufferedReader reader = Files.newBufferedReader(clientIDsFilePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) { // Avoid empty lines
                        clientIDs.add(line.trim()); // Add each client ID to the set
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to persist both jobs and client IDs
    public void persist() {
        persistJobs(); // Persist jobs to JSON
        persistClientIDs(); // Persist client IDs to file
    }

    // New method: Get the count of client IDs
    public int getClientCount() {
        return clientIDs.size();
    }

    // New method: Check if a job exists by jobID
    public boolean containsJob(String jobID) {
        return jobs.containsKey(jobID);
    }

    // New method: Register a new client ID
    public void registerClient(String clientID) {
        if (clientID != null && !clientID.isEmpty()) {
            clientIDs.add(clientID); // Add client ID to the set
            persist(); // Persist jobs and client IDs after registration
        }
    }
}
