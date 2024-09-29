package com.myBackup.server.restapi;

import com.myBackup.models.BackupJob;
import com.myBackup.models.BackupTask;
import com.myBackup.services.JobService;
import com.myBackup.services.TaskQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/job")
public class JobRestController {
    private static final Logger logger = LoggerFactory.getLogger(JobRestController.class);
    
    private final JobService jobService;
    private final TaskQueue taskQueue;
    
    public JobRestController(JobService jobService, TaskQueue taskQueue) {
        this.jobService = jobService;
        this.taskQueue = taskQueue;
    }

    @PostMapping("/")
    public ResponseEntity<BackupJob> createJob(@RequestBody JobRequest jobRequest) {
        // Logger instance for the class
        // Log the JobRequest object
        logger.info("Received JobRequest: {}", jobRequest);

        // Validate the creator field
        if (jobRequest.getCreator() == null || jobRequest.getCreator().trim().isEmpty()) {
            logger.warn("JobRequest validation failed: creator is null or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Map JobRequest to BackupJob
        BackupJob backupJob = new BackupJob();
        backupJob.setJobID(UUID.randomUUID().toString());
        backupJob.setClientIDs(jobRequest.getClientIDs());
        backupJob.setSourceDirectory(jobRequest.getSourceDirectory());
        backupJob.setRepositoryID(jobRequest.getRepositoryID());
        backupJob.setCreator(jobRequest.getCreator());
        backupJob.setCreationTime(Instant.now());
        backupJob.setCronExpression(jobRequest.getCronExpression());
        backupJob.setType(jobRequest.getType());

        // Add the job to the service
        jobService.addJob(backupJob);

        // Log the created BackupJob
        logger.info("Created BackupJob: {}", backupJob);

        // Return the created BackupJob in the response with status 201
        return ResponseEntity.status(HttpStatus.CREATED).body(backupJob);
    }
    @GetMapping("/{jobId}")
    public ResponseEntity<BackupJob> getJob(@PathVariable String jobId) {
        Optional<BackupJob> existingJob = jobService.getJobById(jobId);
        return existingJob.map(ResponseEntity::ok)
                 .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, BackupJob>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/creator/{creatorName}")
    public ResponseEntity<List<BackupJob>> getJobsByCreator(@PathVariable String creatorName) {
        List<BackupJob> jobsByCreator = jobService.getJobsByCreator(creatorName);
        return ResponseEntity.ok(jobsByCreator);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<BackupJob> updateJob(@PathVariable String jobId, @RequestBody BackupJob updatedJob) {
        Optional<BackupJob> existingJob = jobService.getJobById(jobId);
        if (existingJob.isPresent()) {
            jobService.updateJob(jobId, updatedJob);
            return ResponseEntity.ok(updatedJob);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable String jobId) {
        Optional<BackupJob> existingJob = jobService.getJobById(jobId);
        if (existingJob.isPresent()) {
            jobService.deleteJob(jobId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executeJob(@RequestBody Map<String, String> request) {
        String jobID = request.get("jobID");
        
        logger.debug("POST /api/job/execute recieves request: jobID={}", jobID);
        
        if (jobID == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid Job ID (null)");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        //String username = getCurrentUser(); // Get the username from SecurityContextHolder
        Optional<BackupJob> optionalJob = jobService.getJobById(jobID);
        
        if (!optionalJob.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid Job Data: Can't find the job with jobID=" + jobID);
            return ResponseEntity.badRequest().body(errorResponse);        	
        }
        
        BackupTask task = new BackupTask(optionalJob.get());
        taskQueue.add(task);

        // Respond with JSON indicating the job execution has started
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Job execution started for Job ID: " + jobID);
        return ResponseEntity.ok(successResponse);
    }
    
    // Helper method to retrieve the current user's username from the SecurityContext
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null; // Get the current username from the Authentication object
    }
}

