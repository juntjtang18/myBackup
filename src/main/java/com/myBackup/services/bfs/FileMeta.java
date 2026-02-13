package com.myBackup.services.bfs;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class FileMeta implements Serializable {
    private static final long serialVersionUID = 1L; // Unique identifier for serialization
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMeta)) return false;
        FileMeta fileMeta = (FileMeta) o;
        return fileSize == fileMeta.fileSize &&
               Objects.equals(fileName, fileMeta.fileName) &&
               Objects.equals(timestamp, fileMeta.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileSize, timestamp);
    }

    public static Comparator<FileMeta> byFileName() {
        return Comparator.comparing(FileMeta::getFileName);
    }

    public static Comparator<FileMeta> byFileSize() {
        return Comparator.comparingLong(FileMeta::getFileSize);
    }

    public static Comparator<FileMeta> byTimestamp() {
        return Comparator.comparing(FileMeta::getTimestamp);
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
