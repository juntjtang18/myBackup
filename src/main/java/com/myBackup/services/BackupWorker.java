package com.myBackup.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import com.myBackup.models.Task;
import com.myBackup.models.Task.TaskStatus;
import com.myBackup.services.TaskDispatcher.ShutdownEvent;
import com.myBackup.services.bfs.Backup;
import com.myBackup.services.bfs.RepositoryStorage;

import java.io.File;

public class BackupWorker implements Runnable {
    private static final Logger logger = LogManager.getLogger(BackupWorker.class);
    private Task task;
    private ApplicationEventPublisher eventPublisher;
    private RepositoryStorage repoService;
    private volatile boolean running = true; // Control flag
    
    // Constructor
    public BackupWorker(Task task, ApplicationEventPublisher eventPublisher, RepositoryStorage repoService) {
        this.task = task;
        this.eventPublisher = eventPublisher;
        this.repoService = repoService;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Simulate long-running task
                processTask();
                // Break the loop when the task is completed or on shutdown
                running = false; // Set to false if completed normally
            } catch (Exception e) {
                logger.error("Error processing task: {}", task.getTaskId(), e);
                running = false; // Exit on error
            }
        }
        logger.info("Worker for task {} is shutting down.", task.getTaskId());
    }

    private void processTask() throws InterruptedException {
        // Simulate work with potential for long-running task
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000); // Replace with actual task logic
            // Optionally check if running is false to exit early
            int progress = i * 10;
            logger.debug("Task{} is in progress {}", task.getTaskId(), progress);
            task.setStatus(TaskStatus.IN_PROGRESS);
            eventPublisher.publishEvent(new TaskEvent(this, task, progress)); // Report 100% progress
            if (!running) {
                logger.info("Worker for task {} interrupted.", task.getTaskId());
                return;
            }
        }
        logger.info("Backup task {} completed.", task.getTaskId());
        task.setStatus(TaskStatus.COMPLETED);
        eventPublisher.publishEvent(new TaskEvent(this, task, 100)); // Report 100% progress
    }

    private void processTask2() throws InterruptedException {
        String taskId = task.getTaskId();
        try {
            logger.info("Starting backup task: {}", taskId);
            task.setStatus(TaskStatus.IN_PROGRESS);
            eventPublisher.publishEvent(new TaskEvent(this, task, 0)); // Notify start

            String sourceDirectory = task.getSrcDir();
            String repoID = task.getRepoID();
            String destDirectory = repoService.getRepositoryById(repoID).getDestDirectory();
            
            // Check if source directory exists
            File sourceDir = new File(sourceDirectory);
            if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                logger.error("Source directory {} does not exist, exiting backup.", sourceDirectory);
                task.setStatus(TaskStatus.FAILED);
                eventPublisher.publishEvent(new TaskEvent(this, task, 0, "Source directory not found"));
                return;
            }

            // Use BackupRepository to get the last backup size
            long lastBackupSize = 0; //backupRepo.getLastBackupSize(task.getBackupJob().getCreator(), sourceDirectory);
            boolean isFirstBackup = (lastBackupSize <= 0); // Determine if it's the first backup

            long totalDataSize = isFirstBackup ? calculateTotalDataSize(sourceDir) : lastBackupSize;

            // Create a new Backup object
            Backup backup = new Backup("", null, null, sourceDirectory, destDirectory);
            //BackupTree backupTree = backup.getTree();

            // Start processing files
            //processFiles(sourceDir, backupTree, totalDataSize, isFirstBackup);

            // Call the Backup's persist method
            logger.info("Persisting backup for task: {}", taskId);
            //backup.persist();  // This saves the tree file and snapshot file

            // Notify completion
            logger.info("Backup task {} completed.", taskId);
            task.setStatus(TaskStatus.COMPLETED);
            eventPublisher.publishEvent(new TaskEvent(this, task, 100)); // Report 100% progress

        } catch (Exception e) {
            logger.error("Backup task {} failed: {}", taskId, e.getMessage());
            task.setStatus(TaskStatus.FAILED);
            eventPublisher.publishEvent(new TaskEvent(this, task, 0, e.getMessage()));
        }
    }

    private long calculateTotalDataSize(File currentDir) {
        long totalSize = 0;
        File[] files = currentDir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    totalSize += calculateTotalDataSize(file); // Recur for subdirectories
                } else {
                    totalSize += file.length(); // Add file size
                }
            }
        }
        return totalSize;
    }
    
    // Method to listen for the shutdown event
    @EventListener
    public void handleShutdownEvent(ShutdownEvent event) {
        logger.info("Received shutdown event for task {}.", task.getTaskId());
        running = false; // Set running to false to exit the loop
    }

}
