package com.myBackup.services;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;

import com.myBackup.models.Task;
import com.myBackup.models.Task.TaskStatus;


@Service
public class TaskQueueBlockingImpl implements TaskQueue {
    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    @Override
    public void add(Task backupTask) {
    	backupTask.setStatus(TaskStatus.WAITING);
        queue.add(backupTask);
        
    }

    @Override
    public Task take() throws InterruptedException {
        return queue.take(); // Blocks if the queue is empty
    }

    @Override
    public int size() {
        return queue.size();
    }

	@Override
	public void addAll(List<Task> tasks) {
		queue.addAll(tasks);
	}
}
