package com.myBackup.services.bfs;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.myBackup.models.Job;

// Custom exception for directory issues
@SuppressWarnings("serial")
class DirectoryNotFoundException extends RuntimeException {
    public DirectoryNotFoundException(String message) {
        super(message);
    }
}

public class Repository {
    private final String repoID;
    private String destDirectory; // Renamed variable
    private final Map<String, Job> jobs; // Map of jobs
    private final Set<String> clientIDs;
    private String serverUrl; // New attribute
    private String serverName;
    
    // Constructor
    public Repository() {
        this.repoID = UUID.randomUUID().toString();
        this.destDirectory = "";
        this.jobs = Collections.synchronizedMap(new HashMap<>()); // Synchronized map for thread safety
        this.clientIDs = Collections.synchronizedSet(new HashSet<>()); // Synchronized set for thread safety
        this.serverUrl = ""; 
        this.serverName = "";
    }
    
    // Get the destination directory of the repository
    public String getDestDirectory() {
        return destDirectory;
    }

    public void setDestDirectory(String destDirectory) {
        this.destDirectory = destDirectory;
    }

    // Get all jobs (clients) associated with the repository
    public Map<String, Job> getJobs() {
        return jobs;
    }

    // Get the repository ID
    public String getRepoID() {
        return repoID;
    }

    // Get the access list of client IDs
    public Set<String> getClientIDs() {
        return clientIDs;
    }

    // New method: Check if a job exists by jobID
    public boolean hasJob(String jobID) {
        return jobs.containsKey(jobID);
    }

    // Check if the repository is accessible to a specific client
    public boolean isAccessibleTo(String clientID) {
        return clientIDs.contains(clientID);
    }

    // Getter for serverUrl
    public String getServerUrl() {
        return serverUrl;
    }

    // Setter for serverUrl
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

	public String getServerName() {
		return serverName;
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public Map<String, Job> getJobsByCreator(String creator) {
	    // Filtering jobs based on creator
	    return jobs.values().stream()
	            .filter(job -> job.getCreator().equals(creator)) // Assuming BackupJob has getCreator() method
	            .collect(Collectors.toMap(Job::getJobID, job -> job)); // Collecting into a map
	}
}
