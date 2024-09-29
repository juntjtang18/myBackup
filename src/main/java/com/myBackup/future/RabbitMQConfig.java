package com.myBackup.future;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RabbitMQConfig {
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public RabbitMQConfig() {
        factory = new ConnectionFactory();
        loadRabbitMQProperties();
    }

    private void loadRabbitMQProperties() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("rabbitmq.ini")) {
            properties.load(input);
            factory.setHost(properties.getProperty("rabbitmq.host", "localhost")); // Default to localhost
            factory.setPort(Integer.parseInt(properties.getProperty("rabbitmq.port", "5672"))); // Default RabbitMQ port
            factory.setUsername(properties.getProperty("rabbitmq.username", "guest"));
            factory.setPassword(properties.getProperty("rabbitmq.password", "guest"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load RabbitMQ properties from rabbitmq.ini", e);
        }
    }

    public void connect() throws Exception {
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare("backup_tasks", true, false, false, null); // Create the queue
    }

    public Channel getChannel() {
        return channel;
    }

    public void close() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
}
