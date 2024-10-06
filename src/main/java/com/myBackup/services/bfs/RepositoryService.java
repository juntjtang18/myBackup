package com.myBackup.services.bfs;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class RepositoryService {
    
    private Repository repository;
    private FileRefManager fileRef;
    private BlockStorage blockStorage;
    
    public RepositoryService(Repository repository, FileRefManager fileRef, BlockStorage blockStorage) {
        this.repository = repository;
        this.fileRef = fileRef;
        this.blockStorage = blockStorage;
    }

    public boolean fileHashExists(String fileHash) throws IOException {
        return fileHash != null && fileRef.fileHashExists(fileHash);
    }

    public String uploadBlock(String hash, byte[] dataBlock, boolean encrypt) throws NoSuchAlgorithmException, IOException {
        return blockStorage.storeBlock(hash, dataBlock, encrypt);
        //return "exampleBlockHash";
    }

    public boolean blockHashExists(String blockHash) {
        // Placeholder for actual implementation
        return blockHash != null && blockHash.equals("exampleBlockHash");
    }
    // Other methods for repository-specific operations
}
