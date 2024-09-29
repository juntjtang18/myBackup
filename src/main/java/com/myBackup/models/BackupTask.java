package com.myBackup.models;

import java.time.Instant;
import java.util.UUID;

public class BackupTask {
    private String taskId;
    private Instant enqueuedTime;      // Time when the task was added to the queue
    private Instant pickedTime;        // Time when the task was picked up by the worker
    private Instant completedTime;     // Time when the task was completed
    private BackupJob backupJob;
    private TaskStatus status; // New field to track the task state

    public enum TaskStatus {
        WAITING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        UNKNOWN
    }
    // Constructor
    public BackupTask(BackupJob backupJob) {
        this.taskId = UUID.randomUUID().toString();
        this.setBackupJob(backupJob);
        this.status = TaskStatus.UNKNOWN;
    }

    // Getters and Setters
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

	public BackupJob getBackupJob() {
		return backupJob;
	}

	public void setBackupJob(BackupJob backupJob) {
		this.backupJob = backupJob;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}
}
