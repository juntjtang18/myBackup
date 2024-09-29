package com.myBackup.models;

import java.io.Serializable;
import java.util.Date;

public class Backup implements Serializable {
    private String name;
    private String sourceDirectory;
    private String backupDirectory;

    public Backup(String name, String sourceDirectory, String backupDirectory) {
        this.name = name;
        this.sourceDirectory = sourceDirectory;
        this.backupDirectory = backupDirectory;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getBackupDirectory() {
        return backupDirectory;
    }
}
