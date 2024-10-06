package com.myBackup.services.bfs;

import java.util.Set;

public class BackupFile {
    private FileMeta fileMeta;                // File metadata
    private Set<String> blockMap;     // Mapping of block hashes to block data

    public BackupFile() {
    	this.fileMeta = null;
    	this.blockMap = null;
    }
    
    // Constructor
    public BackupFile(FileMeta fileMeta, Set<String> blockMap) {
        this.fileMeta = fileMeta;
        this.blockMap = blockMap;
    }

    // Getters
    public FileMeta getFileMeta() {
        return fileMeta;
    }

    public Set<String> getBlockMap() {
        return blockMap;
    }

    // Other methods for backup operations (e.g., addBlock, removeBlock, etc.)
}
