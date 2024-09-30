package com.myBackup.models;

import java.time.Instant;
import java.util.UUID;

public class Task {
    private String taskId;
    private Instant enqueuedTime;      	// Time when the task was added to the queue
    private Instant pickedTime;        	// Time when the task was picked up by the worker
    private Instant completedTime;     	// Time when the task was completed
    private String jobID;
    private String clientID;			// each job could have multiple tasks if they have multiple clientID. 
    private String creator;				// which means one client has a task explicitly
    private String srcDir;
    private String repoID;
    private String serverUrl;
    private String dstDir;
    private TaskStatus status;

    public enum TaskStatus {
        WAITING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        UNKNOWN
    }

    // Constructor
    public Task() {
        this.taskId = UUID.randomUUID().toString();
        this.status = TaskStatus.UNKNOWN;
    }

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Instant getEnqueuedTime() {
		return enqueuedTime;
	}

	public void setEnqueuedTime(Instant enqueuedTime) {
		this.enqueuedTime = enqueuedTime;
	}

	public Instant getPickedTime() {
		return pickedTime;
	}

	public void setPickedTime(Instant pickedTime) {
		this.pickedTime = pickedTime;
	}

	public Instant getCompletedTime() {
		return completedTime;
	}

	public void setCompletedTime(Instant completedTime) {
		this.completedTime = completedTime;
	}

	public String getJobID() {
		return jobID;
	}

	public void setJobID(String jobID) {
		this.jobID = jobID;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
	}

	public String getRepoID() {
		return repoID;
	}

	public void setRepoID(String repoID) {
		this.repoID = repoID;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getDstDir() {
		return dstDir;
	}

	public void setDstDir(String dstDir) {
		this.dstDir = dstDir;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

}
