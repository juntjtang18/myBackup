package com.myBackup.client.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.myBackup.security.TokenInfo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ServerRegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(ServerRegistrationService.class);

    private final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Change the file location to <user.dir>/config/ServerRegistration.json
    private final String tokenCacheFilePath = Paths.get(System.getProperty("user.dir"), "config", "client", "ServerRegistration.json").toString();
    private final ObjectMapper objectMapper;  // Jackson ObjectMapper for JSON serialization
    private final UUIDService uuidService;
    
    public ServerRegistrationService(ObjectMapper objectMapper, UUIDService uuidService) {
    	this.objectMapper = objectMapper;
    	this.uuidService = uuidService;
    }
    
    @PostConstruct
    public void init() {
        loadTokenCache(); // Load the token cache when the application starts
        logger.debug("Server Registration initialized.");
    }

    @PreDestroy
    public void cleanup() {
        saveTokenCache(); // Save the token cache to the file before the application shuts down
    }

 // Save the token cache to a JSON file using Jackson
    public void saveTokenCache() {
        lock.lock();
        try {
            // Create the parent directory if it doesn't exist
            File file = new File(tokenCacheFilePath);
            File parentDir = file.getParentFile();
            
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // Create the subdirectory and any necessary parent directories
            }

            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile(); // Create the file
            }

            logger.debug("Saving token cache to {}.", tokenCacheFilePath);
            objectMapper.writeValue(file, tokenCache); // Serialize the tokenCache map into JSON and save to file
        } catch (IOException e) {
            logger.error("Error saving token cache to JSON file: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Load the token cache from a JSON file using Jackson
    public void loadTokenCache() {
        lock.lock();
        try {
            File file = new File(tokenCacheFilePath);
            if (file.exists()) {
                Map<String, TokenInfo> loadedCache = objectMapper.readValue(file, new TypeReference<Map<String, TokenInfo>>() {});
                tokenCache.putAll(loadedCache);  // Load token data into the tokenCache map
                logger.debug("Loaded token cache from {}.", tokenCacheFilePath);
            } else {
                logger.warn("Token cache file does not exist: {}", tokenCacheFilePath);
            }
        } catch (IOException e) {
            logger.error("Error loading token cache from JSON file: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Retrieve a new token for a given server address (can be localhost or remote)
    public TokenInfo registerToLocalServer(String serverAddress) {
        logger.debug("ServerRegistration::register invoked for server address: {}", serverAddress);
        
        try {
            // Define the URL for the POST request (no query parameters, everything in body)
            String url = String.format("%s/register-to-server", serverAddress);
            
            // Create a map for the form parameters (uuid and username)
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("uuid", uuidService.getUUID()); // Add the UUID to the request body
            formData.add("username", System.getProperty("user.name")); // Optional two-factor code

            // Log the form data for tracking purposes
            logger.debug("POST {} with Form data {}", url, formData);

            // Send the POST request with form data and expect a TokenInfo response
            TokenInfo response = restTemplate.postForObject(url, formData, TokenInfo.class);
            
            logger.debug("TokenInfo received from server: {}", response);

            // If response is valid, cache the token and save it
            if (response != null) {
                logger.debug("Storing token information in cache for server: {}", serverAddress);
                
                tokenCache.put(serverAddress, response);
                saveTokenCache(); // Save the updated cache to JSON
                
            } else {
                logger.warn("No token information received from server: {}", serverAddress);
            }

            return response;
        } catch (Exception e) {
            logger.error("Error during registration with server {}: {}", serverAddress, e.getMessage(), e);
        }

        logger.debug("Registration process completed for server: {}", serverAddress);
        return null;
    }

    // Check if the access token is expired
    public boolean isTokenExpired(TokenInfo tokenInfo) {
        return Instant.now().getEpochSecond() >= tokenInfo.getAccessTokenExpiresAt().getEpochSecond();
    }

    // Refresh the access token using the refresh token
    public String refreshAccessToken(String serverAddress, String refreshToken) {
        try {
            String url = String.format("http://%s/refreshToken?refreshToken=%s", serverAddress, refreshToken);
            TokenInfo response = restTemplate.getForObject(url, TokenInfo.class);

            if (response != null && response.getAccessToken() != null) {
                return response.getAccessToken();
            }
        } catch (Exception e) {
            logger.error("Error refreshing token for {}: {}", serverAddress, e.getMessage());
        }
        return null;
    }

    // Check if a server is registered
    public boolean isRegistered(String serverAddress) {
        TokenInfo tokenInfo = tokenCache.getOrDefault(serverAddress, null);
        return tokenInfo != null;
    }
}
