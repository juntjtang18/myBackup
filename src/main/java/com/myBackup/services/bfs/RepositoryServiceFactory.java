package com.myBackup.services.bfs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class RepositoryServiceFactory {
	private RepositoryStorage repoStorage;
    private final Map<String, RepositoryService> repositoryServices = new HashMap<>();
    
    @Autowired
    public RepositoryServiceFactory (RepositoryStorage repoStorage) {
    	this.repoStorage = repoStorage;
    }

    // Method to get or create a RepositoryService instance for a specific repository
    public RepositoryService getRepositoryService(String repositoryId) {
        // Check if the repository already exists in the map
        if (!repositoryServices.containsKey(repositoryId)) {
            Repository repository = repoStorage.getRepositoryById(repositoryId);
            
            // Initialize FileRefManager and BlockStorage
            FileRefManager fileRefManager = new FileRefManager(repository.getDestDirectory());
            BlockStorage blockStorage = new BlockStorage(repository.getDestDirectory());

            // Create a new RepositoryService for the repository
            RepositoryService newService = new RepositoryService(repository, fileRefManager, blockStorage);
            repositoryServices.put(repositoryId, newService);
        }
        return repositoryServices.get(repositoryId);
    }
}
