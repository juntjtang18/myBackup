package com.myBackup.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskQueueConfig {

    @Value("${task.queue.type}")
    private String queueType;

    @Bean
    public TaskQueue taskQueue() {
        TaskQueueFactory.QueueType type = TaskQueueFactory.QueueType.valueOf(queueType.toUpperCase());
        return TaskQueueFactory.createQueue(type);
    }
}
