package com.myBackup.services;

import com.myBackup.models.BackupTask;

public interface TaskQueue {
    void add(BackupTask backupTask);
    BackupTask take() throws InterruptedException;
    int size();
}
