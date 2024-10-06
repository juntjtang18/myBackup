package com.myBackup.services.bfs;

import java.util.ArrayList;
import java.util.List;

public class Backup {
    private String commitHash;         // Unique identifier for the backup
    private BackupTree backupTree;     // The tree structure containing the backups
    private String timestamp;           // Timestamp of when the backup was created
    private String description;         // Description or notes about the backup
    private List<String> tags;          // Tags associated with the backup
    private String parentHash;
    
    // Constructor
    public Backup(String commitHash, BackupTree backupTree, String timestamp, String description, String parentHash) {
        this.commitHash = commitHash;
        this.backupTree = backupTree;
        this.timestamp = timestamp;
        this.description = description;
        this.tags = new ArrayList<>();
        this.parentHash = parentHash;
    }

    // Getters
    public String getCommitHash() {
        return commitHash;
    }

    public BackupTree getBackupTree() {
        return backupTree;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    // Method to add a tag
    public void addTag(String tag) {
        tags.add(tag);
    }

    // Method to remove a tag
    public void removeTag(String tag) {
        tags.remove(tag);
    }

	public String getParentHash() {
		return parentHash;
	}

	public void setParentHash(String parentHash) {
		this.parentHash = parentHash;
	}

}
