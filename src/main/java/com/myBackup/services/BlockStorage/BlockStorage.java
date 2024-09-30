package com.myBackup.services.BlockStorage;

import java.io.IOException;

import org.springframework.stereotype.Service;

@Service
public class BlockStorage {
    void saveBlock(String blockId, Block block) throws IOException {
    	
    }
    
    Block loadBlock(String blockId) throws IOException {
    	return null;
    }
    
    boolean blockExists(String blockId) {
    	return false;
    }
}
