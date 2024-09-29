package com.myBackup.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.myBackup.models.BackupTask;


@SuppressWarnings("serial")
public class BackupTaskEvent extends ApplicationEvent {
    private String eventID;
    //private BackupEventType eventType;
    private Instant eventTime;
    private BackupTask backupTask;
    private int progressPercentage;
    private String message;
    
    //public enum BackupEventType {
    //	IN_PROGRESS,
    //	COMPLETED,
    //	FAILED,
    //	PENDING
    //}
    
    // Constructor
    public BackupTaskEvent(Object source, BackupTask backupTask, int progressPercentage) {
        this(source, backupTask, progressPercentage, "");
    }

	public BackupTaskEvent(Object source, BackupTask backupTask, int progressPercentage, String message2) {
		super(source);
        this.eventID = UUID.randomUUID().toString();
    	//this.eventType = eventType;
    	this.eventTime = Instant.now();
    	this.backupTask = backupTask;
    	this.progressPercentage = progressPercentage;
    	this.message = message2;
	}

	public String getEventID() {
		return eventID;
	}

	public void setEventID(String eventID) {
		this.eventID = eventID;
	}

	public Instant getEventTime() {
		return eventTime;
	}

	public void setEventTime(Instant eventTime) {
		this.eventTime = eventTime;
	}

	public BackupTask getBackupTask() {
		return backupTask;
	}

	public void setBackupTask(BackupTask backupTask) {
		this.backupTask = backupTask;
	}

	public int getProgressPercentage() {
		return progressPercentage;
	}

	public void setProgressPercentage(int progressPercentage) {
		this.progressPercentage = progressPercentage;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message == null? "":message;
	}

}
