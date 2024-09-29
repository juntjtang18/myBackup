package com.myBackup.server.restapi;

import java.time.Instant;
import java.util.List;

import com.myBackup.models.BackupJob;

public class JobRequest {
    private String jobID;
    private List<String> clientIDs;
    private String sourceDirectory;
    private String repositoryID;
    private String creator;
    private Instant creationTime;
    private String cronExpression;
    private BackupJob.JobType type; // Use JobType enum from BackupJob

    // Default constructor
    public JobRequest() {}

    // Full constructor
    public JobRequest(String jobID, List<String> clientIDs, String sourceDirectory,
                      String repositoryID, String creator, Instant creationTime,
                      String cronExpression, BackupJob.JobType type) {
        this.jobID = jobID;
        this.clientIDs = clientIDs;
        this.sourceDirectory = sourceDirectory;
        this.repositoryID = repositoryID;
        this.creator = creator;
        this.creationTime = creationTime;
        this.cronExpression = cronExpression;
        this.type = type;
    }

    // Getters and setters

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public List<String> getClientIDs() {
        return clientIDs;
    }

    public void setClientIDs(List<String> clientIDs) {
        this.clientIDs = clientIDs;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getRepositoryID() {
        return repositoryID;
    }

    public void setRepositoryID(String repositoryID) {
        this.repositoryID = repositoryID;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
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

    public BackupJob.JobType getType() {
        return type;
    }

    public void setType(BackupJob.JobType type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return "JobRequest{" +
                "jobID='" + jobID + '\'' +
                ", clientIDs=" + clientIDs +
                ", sourceDirectory='" + sourceDirectory + '\'' +
                ", repositoryID='" + repositoryID + '\'' +
                ", creator='" + creator + '\'' +
                ", creationTime=" + creationTime +
                ", cronExpression='" + cronExpression + '\'' +
                ", type='" + type + '\'' +
                '}';
    }}
