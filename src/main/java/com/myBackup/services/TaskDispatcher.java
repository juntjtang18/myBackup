package com.myBackup.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.Instant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.myBackup.config.Config;
import com.myBackup.models.BackupTask;
import com.myBackup.models.BackupTask.TaskStatus;
import com.myBackup.server.repository.BackupRepositoryService;

@Service
public class TaskDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(TaskDispatcher.class);

    private final TaskQueue taskQueue;
    ExecutorService executorService;
    private ApplicationEventPublisher eventPublisher;
    private BackupRepositoryService repoService;

    public TaskDispatcher(TaskQueue taskQueue, ApplicationEventPublisher eventPublisher, BackupRepositoryService repoService) {
        this.taskQueue = taskQueue;
        this.eventPublisher = eventPublisher;
        this.repoService = repoService;

        // Get thread pool size from configuration
        Config config = Config.getInstance();
        int threadPoolSize = config.getThreadPoolSize();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize); // Use configurable size
    }

    @PostConstruct
    public void init() {
        // Start processing tasks when the application starts
    	logger.info("TaskDispatcher starts watching on task queue...");
        start();
    }

    public void start() {
        // Start processing tasks in a new thread
        new Thread(() -> {
            while (true) {
                try {
                    BackupTask task = taskQueue.take(); // Wait for a task to be available
                    task.setPickedTime(Instant.now());
                    task.setStatus(TaskStatus.IN_PROGRESS);
                    
                    logger.debug("TaskDispatcher picked up a task from TaskQueue: {}", task.getTaskId());
                    
                    //executorService.submit(new BackupWorker(task, eventPublisher)); // Execute task
                    
                    executorService.submit(new BackupWorker(task, eventPublisher, repoService)); // Execute task
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break; // Exit loop on interruption
                }
            }
        }).start();
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down TaskDispatcher...");

        // Initiate shutdown of the executor service
        executorService.shutdown(); // Stop accepting new tasks
        eventPublisher.publishEvent(new ShutdownEvent(this)); // Notify workers to shut down

        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) { // Wait for 60 seconds
                logger.warn("Executor did not terminate in the specified time. Forcing shutdown...");
                executorService.shutdownNow(); // Cancel currently executing tasks
            }
        } catch (InterruptedException ex) {
            logger.error("Shutdown interrupted", ex);
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow(); // Force shutdown
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }

        logger.info("TaskDispatcher shut down successfully.");
    }

    public class ShutdownEvent extends ApplicationEvent {
		private static final long serialVersionUID = 1L;

		public ShutdownEvent(Object source) {
            super(source);
        }
    }
}
