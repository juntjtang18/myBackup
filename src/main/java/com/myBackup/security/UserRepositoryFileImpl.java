package com.myBackup.security;

import com.myBackup.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class UserRepositoryFileImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryFileImpl.class);
    private final Config config;
    private Map<String, User> users = new ConcurrentHashMap<>(); // Use ConcurrentHashMap
    private final ReentrantLock lock = new ReentrantLock(); // Lock for more complex operations
    private long lastModifiedTime = 0;

    public UserRepositoryFileImpl(Config config) {
        this.config = config;
    }

    private String getUsersFilePath() {
        return config.getUsersFilePath();
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) throws IOException {
        loadUsers();
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public void save(User user) throws IOException {
        lock.lock(); // Acquire lock for critical section
        try {
            String usersFilePath = getUsersFilePath();
            Path filePath = Paths.get(usersFilePath);
            Path directoryPath = filePath.getParent();

            if (directoryPath != null && !Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            if (users.putIfAbsent(user.getUsername(), user) != null) {
                throw new IllegalArgumentException("User already exists");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFilePath, true))) {
                writer.write(String.format("%s:%s:%s:%s%n", user.getUsername(), user.getEncryptedPassword(), user.getRole(), user.getEmail()));
                writer.flush();
            }
        } finally {
            lock.unlock(); // Ensure the lock is released
        }
    }

    @Override
    public void updateUser(User updatedUser) throws IOException {
        lock.lock(); // Acquire lock for critical section
        try {
            if (!users.containsKey(updatedUser.getUsername())) {
                throw new IllegalArgumentException("User not found");
            }

            users.put(updatedUser.getUsername(), updatedUser);
            saveUsersToFile();
        } finally {
            lock.unlock(); // Ensure the lock is released
        }
    }

    private synchronized void saveUsersToFile() throws IOException {
        String usersFilePath = getUsersFilePath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFilePath))) {
            for (User user : users.values()) {
                writer.write(String.format("%s:%s:%s:%s%n", user.getUsername(), user.getEncryptedPassword(), user.getRole(), user.getEmail()));
            }
        }
    }

    private void loadUsers() throws IOException {
        lock.lock(); // Acquire lock for critical section
        try {
            String usersFilePath = getUsersFilePath();
            File usersFile = new File(usersFilePath);
            long currentModifiedTime = usersFile.lastModified();

            if (currentModifiedTime > lastModifiedTime) {
                logger.debug("Users file modified, reloading users...");
                users.clear(); // Clearing the map is safe here due to the lock
                try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 4) {
                            users.put(parts[0], new User(parts[0], parts[1], parts[2], parts[3]));
                        }
                    }
                }
                lastModifiedTime = currentModifiedTime;
            } else {
                logger.debug("Users file not modified, using cached data.");
            }
        } finally {
            lock.unlock(); // Ensure the lock is released
        }
    }
    
    @Override
    public List<User> loadAllUsers() throws IOException {
        loadUsers();
        return new ArrayList<>(users.values()); // Return a copy of the values in the map
    }   

}

