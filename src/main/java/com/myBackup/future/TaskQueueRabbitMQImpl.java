package com.myBackup.future;

import com.myBackup.models.Task;
import com.myBackup.services.TaskQueue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class TaskQueueRabbitMQImpl implements TaskQueue {
    private final RabbitMQConfig rabbitMQConfig;
    private final Channel channel;

    public TaskQueueRabbitMQImpl(RabbitMQConfig rabbitMQConfig) throws IOException, TimeoutException {
        this.rabbitMQConfig = rabbitMQConfig;
        this.channel = rabbitMQConfig.getChannel();
    }

    @Override
    public void add(Task task) {
        try {
            String serializedTask = serializeTask(task);
            channel.basicPublish("", "backup_tasks", MessageProperties.PERSISTENT_TEXT_PLAIN, serializedTask.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to add task to RabbitMQ", e);
        }
    }

    @Override
    public Task take() {
        final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(1);

        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                Task task = deserializeTask(message);
                taskQueue.offer(task);  // Add task to the queue
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume("backup_tasks", false, deliverCallback, consumerTag -> {});
            return taskQueue.take();  // Blocks until a task is added to the queue

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to retrieve task from RabbitMQ", e);
        }
    }

    @Override
    public int size() {
        try {
            return (int) channel.messageCount("backup_tasks");
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve task queue size", e);
        }
    }

    public void shutdown() {
        try {
            rabbitMQConfig.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close RabbitMQ connection", e);
        }
    }

    private String serializeTask(Task task) {
        // Serialize the BackupTask object to a JSON string or any other format
        return task.toString(); // Example, customize the serialization logic as needed
    }

    private Task deserializeTask(String message) {
        // Deserialize the message back to a BackupTask object
        return new Task(); // Example, customize the deserialization logic as needed
    }

	@Override
	public void addAll(List<Task> tasks) {
		// TODO Auto-generated method stub
		
	}
}
