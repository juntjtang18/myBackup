package com.myBackup.client.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UUIDService {
    private static final String UUID_FILE_PATH = Paths.get(System.getProperty("user.dir"), "config", "client", "uuid.id").toString();
    private final String uuid;

    public UUIDService() {
        this.uuid = loadOrGenerateUUID();
    }

    private String loadOrGenerateUUID() {
        File uuidFile = new File(UUID_FILE_PATH);
        // Ensure the config directory exists
        File parentDir = uuidFile.getParentFile();
        if (!parentDir.exists()) {
        	parentDir.mkdir(); // Create the config directory if it doesn't exist
        }

        if (uuidFile.exists() && uuidFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(uuidFile))) {
                return reader.readLine(); // Read existing UUID from the file
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception as needed
            }
        }
        return generateAndSaveUUID(uuidFile);
    }

    private String generateAndSaveUUID(File uuidFile) {
        String newUUID = UUID.randomUUID().toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(uuidFile))) {
            writer.write(newUUID); // Save new UUID to the file
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception as needed
        }
        return newUUID;
    }

    public String getUUID() {
        return uuid; // Return the UUID, which is immutable for the app's lifecycle
    }
}
