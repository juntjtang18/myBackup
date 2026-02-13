package com.myBackup.services.bfs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RepositoryServiceFactory {
    @Autowired
    private RepositoryManager repoStorage;
    
    // Use a thread-safe ConcurrentMap instead of HashMap
    private final ConcurrentMap<String, RepositoryService> repositoryServices = new ConcurrentHashMap<>();

    public RepositoryService getRepositoryService(String repositoryId) throws IOException {
        // First, check if the repository service is already present without locking
        RepositoryService service = repositoryServices.get(repositoryId);

        // If it's not found, synchronize and check again before creating it
        if (service == null) {
            synchronized (this) {
                service = repositoryServices.get(repositoryId); // Double-check
                if (service == null) {
                    // Retrieve repository from the repoStorage
                    Repository repository = repoStorage.getRepositoryById(repositoryId);
                    
                    // Initialize FileRefManager and BlockStorage
                    FileRefManager fileRefManager = new FileRefManager(repository.getDestDirectory());
                    BlockStorage blockStorage = new BlockStorage(repository.getDestDirectory());

                    // Create a new RepositoryService for the repository
                    service = new RepositoryService(repository, fileRefManager, blockStorage);
                    
                    // Add it to the map
                    repositoryServices.put(repositoryId, service);
                }
            }
        }
        
        return service;
    }
}

