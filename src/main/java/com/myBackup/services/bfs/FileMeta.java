package com.myBackup.services.bfs;

public class FileMeta {
    private String fileName;   // Name of the file being backed up
    private long fileSize;      // Size of the file
    private String timestamp;   // Timestamp of the backup

    // Constructor
    public FileMeta(String fileName, long fileSize, String timestamp) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.timestamp = timestamp;
    }
    
    public FileMeta() {
        this.fileName = null;
        this.fileSize = 0;
        this.timestamp = null;
    }

    // Getters and setters
    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // toString method
    @Override
    public String toString() {
        return "FileMeta{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
