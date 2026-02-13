package com.myBackup.services.bfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

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
    }
    
    public byte[] readBlock(String hash, boolean encrypt) throws IOException, NoSuchAlgorithmException {
        if (blockStorage.blockExists(hash)) {
            return blockStorage.readBlock(hash); // Read the block from storage
        } else {
            throw new FileNotFoundException("Block with hash " + hash + " does not exist.");
        }
    }
    
    public boolean blockHashExists(String blockHash) {
        // Placeholder for actual implementation
        return blockHash != null && blockHash.equals("exampleBlockHash");
    }

    public boolean commitAFile(String hash, BackupFile backupFile) throws IOException {
    	if (verifyBackupFileBlocks(backupFile)) {
    		fileRef.saveHashMapping(hash, backupFile);
    		return true;
    	} else { 
    		return false;
    	}
    }
    
    public Optional<BackupFile> getBackupFile(String hash) throws IOException {
        return fileRef.readHashMapping(hash);
    }

    public boolean verifyBackupFileBlocks(BackupFile backupFile) throws IOException {
        for (String blockHash : backupFile.getBlockMap()) {
            if (!blockStorage.blockExists(blockHash)) {
                return false; // Return false if any block does not exist
            }
        }
        return true; // All blocks exist    
    }
}
