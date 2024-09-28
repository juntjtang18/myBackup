package com.myBackup.security;

import com.myBackup.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryFileImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryFileImpl.class);

    private final Config config;
    private List<User> users = new ArrayList<>();
    private long lastModifiedTime = 0;  // Track last modified time of users.pwd

    public UserRepositoryFileImpl(Config config) {
        this.config = config;
    }

    private String getUsersFilePath() {
        return config.getUsersFilePath(); // Retrieve the path from the configuration
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) throws IOException {
        loadUsers();
        return users.stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst();
    }

    @Override
    public synchronized void save(User user) throws IOException {
        String usersFilePath = getUsersFilePath();
        Path filePath = Paths.get(usersFilePath);
        Path directoryPath = filePath.getParent(); // Get the directory path

        // Check if the directory exists; if not, create it
        if (directoryPath != null && !Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Check if the file exists; if not, create it
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFilePath, true))) {
            // Check if user already exists
            for (User existingUser : users) {
                if (existingUser.getUsername().equals(user.getUsername())) {
                    throw new IllegalArgumentException("User already exists");
                }
            }
            // Add the new user
            writer.write(String.format("%s:%s:%s:%s%n", user.getUsername(), user.getEncryptedPassword(), user.getRole(), user.getEmail()));
            writer.flush();

            // Add to in-memory cache
            users.add(user);
        }
    }
    
    @Override
    public void create() throws IOException {
        String usersFilePath = getUsersFilePath();
        Path filePath = Paths.get(usersFilePath);

        // Create the users file if it doesn't exist
        File file = filePath.toFile();
        if (!file.exists()) {
            logger.debug("Creating new users file: {}", usersFilePath);
            file.createNewFile();
        }
    }

    private synchronized void loadUsers() throws IOException {
        String usersFilePath = getUsersFilePath();
        File usersFile = new File(usersFilePath);
        long currentModifiedTime = usersFile.lastModified();

        // Check if file has been updated since last load
        if (currentModifiedTime > lastModifiedTime) {
            logger.debug("Users file modified, reloading users...");
            users.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 4) {
                        users.add(new User(parts[0], parts[1], parts[2], parts[3]));
                    }
                }
            }
            lastModifiedTime = currentModifiedTime;  // Update the last modified time
        } else {
            logger.debug("Users file not modified, using cached data.");
        }
    }
    
    @Override
    public List<User> loadAllUsers() throws IOException {
        loadUsers(); // Ensure the users are loaded or reloaded from the file
        return new ArrayList<>(users); // Return a copy of the users list to avoid direct manipulation
    }
}
