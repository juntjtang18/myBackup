package com.myBackup.server.meta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBackup.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ServersService {
    private final String serversFilePath;
    private final ObjectMapper objectMapper;
    private List<Server> servers;
    private Config config;
    public ServersService(Config config) {
    	this.config = config;
        this.serversFilePath = this.config.getServersFilePath();
        this.objectMapper = new ObjectMapper();
        this.servers = new ArrayList<>();
        loadServers();
    }

    // Load servers from the specified file
    private void loadServers() {
        File file = new File(serversFilePath);
        // Check if the directory exists; if not, create it
        if (!Files.exists(Paths.get(serversFilePath).getParent())) {
            try {
                Files.createDirectories(Paths.get(serversFilePath).getParent()); // Create the directory if it doesn't exist
            } catch (IOException e) {
                e.printStackTrace();
                return; // Exit if the directory cannot be created
            }
        }

        // Check if the file exists; if not, create it
        if (!file.exists()) {
            try {
                file.createNewFile();
                // Add default server record if it's not already present
                addDefaultServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Server[] loadedServers = objectMapper.readValue(file, Server[].class);
                for (Server server : loadedServers) {
                    servers.add(server);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to add a default server if it does not already exist
    private void addDefaultServer() {
        String defaultServerUrl = "localhost:8080";
        String defaultAliasName = "My Machine";

        // Check if the default server is already present
        boolean exists = servers.stream().anyMatch(server -> 
            server.getServerUrl().equals(defaultServerUrl) && 
            server.getAliasName().equals(defaultAliasName));

        if (!exists) {
            // Add the default server
            addServer(defaultServerUrl, defaultAliasName);
        }
    }

    // Save servers to the specified file
    public void saveServers() {
        try {
            // Check if the directory exists; if not, create it
            Path path = Paths.get(serversFilePath);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent()); // Create the directory if it doesn't exist
            }
            objectMapper.writeValue(new File(serversFilePath), servers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<Server> getServers() {
    	return servers;
    }

    // Add a new server to the list
    public void addServer(String serverUrl, String aliasName) {
        Server newServer = new Server(serverUrl, aliasName);
        servers.add(newServer);
        saveServers(); // Save changes after adding the new server
    }

    // Define the Server class inline
    public static class Server {
        private String serverUrl;
        private String aliasName;

        public Server() {
            // Default constructor for Jackson
        }

        public Server(String serverUrl, String aliasName) {
            this.serverUrl = serverUrl;
            this.aliasName = aliasName;
        }

        // Getters and setters
        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getAliasName() {
            return aliasName;
        }

        public void setAliasName(String aliasName) {
            this.aliasName = aliasName;
        }

        @Override
        public String toString() {
            return "Server{" +
                    "serverUrl='" + serverUrl + '\'' +
                    ", aliasName='" + aliasName + '\'' +
                    '}';
        }
    }

    // Additional methods can be added for further functionalities, like retrieving servers, removing a server, etc.
}
