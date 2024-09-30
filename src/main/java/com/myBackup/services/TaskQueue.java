package com.myBackup.services;

import java.util.List;

import com.myBackup.models.Task;

public interface TaskQueue {
    void add(Task backupTask);
    Task take() throws InterruptedException;
    int size();
	void addAll(List<Task> tasks);
}
