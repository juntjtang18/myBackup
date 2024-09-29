package com.myBackup.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;

import com.myBackup.models.BackupTask;
import com.myBackup.models.BackupTask.TaskStatus;


@Service
public class TaskQueueBlockingImpl implements TaskQueue {
    private final BlockingQueue<BackupTask> queue = new LinkedBlockingQueue<>();

    @Override
    public void add(BackupTask backupTask) {
    	backupTask.setStatus(TaskStatus.WAITING);
        queue.add(backupTask);
        
    }

    @Override
    public BackupTask take() throws InterruptedException {
        return queue.take(); // Blocks if the queue is empty
    }

    @Override
    public int size() {
        return queue.size();
    }
}
