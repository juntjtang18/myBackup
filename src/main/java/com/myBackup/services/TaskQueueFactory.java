package com.myBackup.services;

import com.myBackup.future.RabbitMQConfig;
import com.myBackup.future.TaskQueueRabbitMQImpl;

public class TaskQueueFactory {
    public enum QueueType {
        BLOCKING,
        RABBITMQ
    }

    public static TaskQueue createQueue(QueueType type) {
        switch (type) {
            case RABBITMQ:
                RabbitMQConfig config = new RabbitMQConfig();
                try {
                    config.connect();
                    return new TaskQueueRabbitMQImpl(config);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create RabbitMQ queue", e);
                }
            case BLOCKING:
            default:
                return new TaskQueueBlockingImpl();
        }
    }
}
