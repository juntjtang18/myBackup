package com.myBackup.services.bfs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class RepositoryServiceFactory {
	@Autowired
	private RepositoryStorage repoStorage;
    private final Map<String, RepositoryService> repositoryServices = new HashMap<>();
    
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
