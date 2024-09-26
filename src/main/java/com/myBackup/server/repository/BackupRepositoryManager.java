package com.myBackup.server.repository;

import java.util.List;

/**
 * Interface for managing backup repositories.
 */
public interface BackupRepositoryManager {

    /**
     * Creates a new repository for a specified destination directory.
     *
     * @param destDir the destination directory for the backup repository
     * @param clientID the ID of the client creating the repository
     * @return the created BackupRepository instance
     * @throws IllegalArgumentException if destDir or clientID is null or invalid
     */
    BackupRepository create(String destDir, String clientID);
    
    /**
     * Creates a new repository based on the provided BackupRepository object.
     *
     * @param repo the BackupRepository object containing the repository details
     * @return the created BackupRepository instance
     * @throws IllegalArgumentException if the provided BackupRepository is null or invalid
     */
    BackupRepository create(BackupRepository repo);

    /**
     * Retrieves all repositories that a specified client ID has access to.
     *
     * @param clientID the ID of the client whose accessible repositories are to be retrieved
     * @return a list of BackupRepository instances accessible by the client
     */
    List<BackupRepository> getAllByClientID(String clientID);
    
    /**
     * Retrieves all available backup repositories.
     *
     * @return a list of all BackupRepository instances
     */
    List<BackupRepository> getAll();
    
    /**
     * Deletes a repository based on the specified repository ID.
     *
     * @param repoID the ID of the repository to be deleted
     * @return true if the repository was successfully deleted, false otherwise
     */
    boolean delete(String repoID);
   
    /**
     * Saves all repository metadata into a JSON file (repositories.json) 
     * in the application root directory.
     *
     * @return true if the metadata was successfully persisted, false otherwise
     */
    boolean persist();  
    
    /**
     * Checks whether a repository with the specified ID exists.
     *
     * @param repoID the ID of the repository to check
     * @return true if the repository exists, false otherwise
     */
    boolean has(String repoID);
}
