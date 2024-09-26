package com.myBackup.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBackup.client.ClientInfo;
import com.myBackup.security.TokenInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RegisteredClientsService {
    private static final Logger logger = LoggerFactory.getLogger(RegisteredClientsService.class);
    private final Map<String, ClientInfo> clientCache = new ConcurrentHashMap<>();
    private ObjectMapper objectMapper;
    private final String clientsFilePath = Paths.get(System.getProperty("user.dir"), "clients.json").toString();
    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    public RegisteredClientsService(ObjectMapper objectMapper) {
    	this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void init() {
        loadClients(); // Load clients from the file into cache
        logger.debug("Registered Clients Service initialized.");
    }

    @PreDestroy
    public void cleanup() {
        saveClients(); // Save clients back to the file if necessary
    }

    // Load clients from clients.json into the cache
    private void loadClients() {
        lock.lock();
        try {
            File file = new File(clientsFilePath);
            if (file.exists()) { // Check if the file exists before loading
                ClientInfo[] clients = objectMapper.readValue(file, ClientInfo[].class);
                for (ClientInfo client : clients) {
                    clientCache.put(client.getUuid(), client);
                }
                logger.debug("Loaded {} clients from {}", clientCache.size(), clientsFilePath);
            } else {
                logger.warn("Client file does not exist: {}", clientsFilePath);
            }
        } catch (IOException e) {
            logger.error("Error loading clients from file: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Save clients from the cache to clients.json
    public void saveClients() {
        lock.lock();
        try {
            objectMapper.writeValue(new File(clientsFilePath), clientCache.values());
            logger.debug("Saved {} clients to {}", clientCache.size(), clientsFilePath);
        } catch (IOException e) {
            logger.error("Error saving clients to file: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Check if a client exists in the cache
    public boolean clientExists(String clientId) {
        return clientCache.containsKey(clientId);
    }

    // Get client information by client ID
    public ClientInfo getClientById(String clientId) {
        return clientCache.get(clientId);
    }

    public TokenInfo registerLocalClient(String uuid, String username) {
        logger.debug("Registering local client: {}", uuid);
        ClientInfo clientInfo = new ClientInfo(uuid, username, "");
        clientCache.put(uuid, clientInfo); // Add to cache
        saveClients();
        return clientInfo.getTokenInfo();
    }

    public TokenInfo registerRemoteClient(String uuid, String username, String email) {
        // TODO: need to revise in the future. Now there is no authentication. 
        logger.debug("Registering remote client: {}", uuid);
        ClientInfo clientInfo = new ClientInfo(uuid, username, email);
        clientCache.put(uuid, clientInfo); // Add to cache
        return clientInfo.getTokenInfo();
    }
}
