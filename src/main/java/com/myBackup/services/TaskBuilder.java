package com.myBackup.services;

import com.myBackup.models.Job;
import com.myBackup.models.Task;
import com.myBackup.services.bfs.Repository;
import com.myBackup.services.bfs.RepositoryStorage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskBuilder {
	@Autowired
	private RepositoryStorage backupRepositoryService;
    private Job job;

    public TaskBuilder withBackupJob(Job job) {
        this.job = job;
        return this;
    }

    public List<Task> build() {
        // Ensure backupJob is not null
        if (job == null) {
            throw new IllegalArgumentException("BackupJob must not be null");
        }
        String repoID 		= job.getRepositoryID();
        Repository repo = backupRepositoryService.getRepositoryById(repoID);
        if (repo == null) {
        	throw new IllegalArgumentException("Job has invalid repository{" + repoID + "}.");
        }
        
        Instant currentTime = Instant.now();
        String jobID 		= job.getJobID();
        String creator      = job.getCreator();
        String srcDir 		= job.getSourceDirectory();
        // Retrieve server URL and destination directory from BackupRepositoryService
        String serverUrl = repo.getServerUrl();
        String dstDir 	= repo.getDestDirectory();
        
        List<Task> tasks = new ArrayList<>();

        // Iterate through each client ID in the BackupJob and create a BackupTask for each
        for (String clientId : job.getClientIDs()) {
            Task task = new Task();
            // taskID has been generated in BackupTask constructor
            task.setEnqueuedTime(currentTime); // Set the current time as enqueued time
            // pickedTime leave to be null
            // completedTime leaves to be null
            task.setJobID(jobID);
            task.setClientID(clientId); // Set the client ID for the task
            task.setCreator(creator);
            task.setSrcDir(srcDir);
            task.setRepoID(repoID);
            task.setServerUrl(serverUrl); // Set server URL from BackupRepositoryService
            task.setDstDir(dstDir); // Set destination directory from BackupRepositoryService
            task.setStatus(Task.TaskStatus.WAITING); // Initialize the task status
            
            tasks.add(task); // Add the task to the list
        }
            
        return tasks; // Return the list of tasks
    }
}
