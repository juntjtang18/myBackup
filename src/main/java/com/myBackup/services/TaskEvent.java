package com.myBackup.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.myBackup.models.Task;


@SuppressWarnings("serial")
public class TaskEvent extends ApplicationEvent {
    private String 	eventID;
    private Instant eventTime;
    private Task 	backupTask;
    private int 	progressPercentage;
    private String 	message;
    
    // Constructor
    public TaskEvent(Object source, Task backupTask, int progressPercentage) {
        this(source, backupTask, progressPercentage, "");
    }

	public TaskEvent(Object source, Task backupTask, int progressPercentage, String message2) {
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

	public Task getBackupTask() {
		return backupTask;
	}

	public void setBackupTask(Task backupTask) {
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
