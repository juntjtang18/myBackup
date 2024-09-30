package com.myBackup.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Job {
    private String 		jobID;
    private List<String> clientIDs;   
    private String 		sourceDirectory;
    private String 		repositoryID;
    private String 		creator; // Mandatory
    private Instant 	creationTime; // Mandatory
    private String 		cronExpression; // Crontab expression for scheduling
    private JobType 	type;

    public Job() {
        this.jobID = UUID.randomUUID().toString();     // Generate unique job ID    	
    }
    
    public enum JobType {
    	MUST_DO,
    	REGULAR
    }

    // Constructor with only the mandatory fields: creator and creationTime
    public Job(List<String> clientIDs) {
        this.jobID = UUID.randomUUID().toString();     // Generate unique job ID
        this.creationTime = Instant.now(); 
        this.type = JobType.REGULAR;
    }

    // Full constructor for cases where all fields are provided
    public Job(List<String> clientIDs, 
    					String sourceDirectory, 
    					String repositoryID,
    					String creator, 
    					String cronExpression, 
    					JobType type) 
    {
        this.jobID = UUID.randomUUID().toString();
        this.sourceDirectory = sourceDirectory;
        this.setRepositoryID(repositoryID);
        this.creator = creator;  // Mandatory
        this.creationTime = Instant.now();  // Mandatory
        this.cronExpression = cronExpression;
    }

    // Getters and setters for all fields

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        if (creator == null || creator.trim().isEmpty()) {
            throw new IllegalArgumentException("Creator cannot be null or empty");
        }
        this.creator = creator;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

	public JobType getType() {
		return type;
	}

	public void setType(JobType type) {
		this.type = type;
	}

	public List<String> getClientIDs() {
		return clientIDs;
	}

	public void setClientIDs(List<String> clientIDs) {
		this.clientIDs = clientIDs;
	}

	public String getRepositoryID() {
		return repositoryID;
	}

	public void setRepositoryID(String repositoryID) {
		this.repositoryID = repositoryID;
	}
	@Override
	public String toString() {
	    return "BackupJob{" +
	            "jobID='" + jobID + '\'' +
	            ", clientIDs=" + clientIDs +
	            ", sourceDirectory='" + sourceDirectory + '\'' +
	            ", repositoryID='" + repositoryID + '\'' +
	            ", creator='" + creator + '\'' +
	            ", creationTime=" + creationTime +
	            ", cronExpression='" + cronExpression + '\'' +
	            ", type='" + type + '\'' +
	            '}';
	}
}
