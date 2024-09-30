package com.myBackup.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBackup.models.Job;

import com.myBackup.config.Config; // Assuming Config is in this package
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final ObjectMapper objectMapper;
    private final Map<String, Job> jobMap; // Map for jobs
    private Map<String, List<String>> creatorIndex; // Map to index jobs by creator
    private final String filePath;
    private final Config config;
    
    public JobService(Config config, ObjectMapper objectMapper) {
    	this.objectMapper = objectMapper;
    	this.config = config;
    	this.filePath = this.config.getJobFilePath(); // Get the job file path from Config
        this.jobMap = new ConcurrentHashMap<>(loadJobsFromFile());
        this.creatorIndex = buildCreatorIndex(jobMap); // Build the index
    }
    
    private void saveJobsToFile() {
        createFileIfNotExists(filePath); // Ensure the file and directory exist before saving
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(objectMapper.writeValueAsString(new ArrayList<>(jobMap.values()))); // Write all jobs as a single array
        } catch (IOException e) {
            e.printStackTrace(); // Handle the error appropriately (consider logging)
        }
    }

    private void createFileIfNotExists(String filePath) {
        Path path = Paths.get(filePath);
        try {
            // Create the directory if it does not exist
            Files.createDirectories(path.getParent());

            // Create the file if it does not exist
            if (!Files.exists(path)) {
                Files.createFile(path);
                // Initialize the file with an empty JSON array to avoid JSON parsing issues
                Files.writeString(path, "[]");
            }
            
        } catch (IOException e) {
            e.printStackTrace(); // Handle the error appropriately (consider logging)
        }
    }

    private Map<String, Job> loadJobsFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return Collections.emptyMap(); // Return an empty map if the file doesn't exist
        }
        try {
            // Load jobs and create a map with jobID as the key
            List<Job> jobList = objectMapper.readValue(file, new TypeReference<List<Job>>() {});
            return jobList.stream().collect(Collectors.toMap(Job::getJobID, job -> job));
        } catch (IOException e) {
            e.printStackTrace(); // Handle the error appropriately (consider logging)
            return Collections.emptyMap();
        }
    }
    
    private Map<String, List<String>> buildCreatorIndex(Map<String, Job> jobMap) {
        Map<String, List<String>> index = new ConcurrentHashMap<>();
        for (Job job : jobMap.values()) {
            index.computeIfAbsent(job.getCreator().toLowerCase(), k -> new java.util.ArrayList<>())
                 .add(job.getJobID());
        }
        return index;
    }

    public Map<String, Job> getAllJobs() {
        return Collections.unmodifiableMap(jobMap); // Return an unmodifiable view of the map
    }

    public Optional<Job> getJobById(String jobId) {
        return Optional.ofNullable(jobMap.get(jobId)); // Get job by ID
    	//return jobMap.get(jobId);
    }

    public List<Job> getJobsByCreator(String creatorName) {
        List<String> jobIds = creatorIndex.getOrDefault(creatorName.toLowerCase(), Collections.emptyList());
        return jobIds.stream()
                     .map(jobMap::get)
                     .collect(Collectors.toList());
    }
    
    public List<Job> getByRepositoryID(String repositoryID) {
        return jobMap.values().stream()
                .filter(job -> job.getRepositoryID().equals(repositoryID))
                .collect(Collectors.toList());
    }

    public void addJob(Job job) {
        jobMap.put(job.getJobID(), job); // Use job ID as key
        indexJobByCreator(job); // Update the creator index
        saveJobsToFile();
    }

    public void updateJob(String jobId, Job updatedJob) {
        if (jobMap.containsKey(jobId)) {
            // Remove the old job from the creator index
            removeJobFromCreatorIndex(jobId);
            jobMap.put(jobId, updatedJob); // Update job in the map
            indexJobByCreator(updatedJob); // Update the creator index
            saveJobsToFile();
        }
    }

    public void deleteJob(String jobId) {
        Job removedJob = jobMap.remove(jobId); // Remove job from the map
        if (removedJob != null) {
            removeJobFromCreatorIndex(jobId); // Update the creator index
        }
        saveJobsToFile();
    }

    private void indexJobByCreator(Job job) {
        creatorIndex.computeIfAbsent(job.getCreator().toLowerCase(), k -> new java.util.ArrayList<>())
                    .add(job.getJobID());
    }

    private void removeJobFromCreatorIndex(String jobId) {
        jobMap.values().stream()
              .filter(job -> job.getJobID().equals(jobId))
              .findFirst()
              .ifPresent(job -> {
                  List<String> jobIds = creatorIndex.get(job.getCreator().toLowerCase());
                  if (jobIds != null) {
                      jobIds.remove(jobId);
                      if (jobIds.isEmpty()) {
                          creatorIndex.remove(job.getCreator().toLowerCase());
                      }
                  }
              });
    }

}
