package com.myBackup.services.bfs;

import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

public class BlockStorage {
    private static final Logger logger = LoggerFactory.getLogger(BlockStorage.class);
	
    private String BLOCKS_DIR;
    private Encryptor encryptor;

    public BlockStorage(String repoDir) {
        this.BLOCKS_DIR = Paths.get(repoDir, "blocks").toString();
        initializeDirectoryStructure();
    }

    private void initializeDirectoryStructure() {
        // Create the base blocks directory first
        Path blocksPath = Paths.get(BLOCKS_DIR);
        try {
            // Check if the directory exists before creating it
            if (!Files.exists(blocksPath)) {
                Files.createDirectories(blocksPath);                
                logger.debug("Blocks directory created successfully at ", blocksPath);
            } else {
                logger.debug("Blocks directory already exists.", blocksPath);
            }
        } catch (IOException e) {
            logger.error("Error creating blocks directory(", blocksPath, ": ", e.getMessage());
        }
    }


    public String storeBlock(String hash, byte[] blockData, boolean encrypt) throws IOException, NoSuchAlgorithmException {
        String filePath = getBlockFilePath(hash);
        Block block = new Block(blockData, encrypt, hash);

        // Ensure the parent directories exist
        File file = new File(filePath);
        File parentDir = file.getParentFile(); // Get the parent directory

        if (!parentDir.exists()) {
            parentDir.mkdirs(); // Create the parent directories if they don't exist
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            // Move the file pointer to the end of the file for appending
            raf.seek(raf.length());

            long offset = raf.getFilePointer(); // Get the current file pointer position (after seeking to the end)
            block.writeTo(raf);
            updateIndex(hash, filePath, offset); // Update index with the file path and offset
        }
        // Verify the hash here, if needed
        return hash;
    }

    public byte[] readBlock(String hash) throws IOException, NoSuchAlgorithmException {
        String[] indexEntry = findIndexEntry(hash);
        if (indexEntry == null) throw new FileNotFoundException("Block with hash " + hash + " not found.");

        String filePath = indexEntry[1];
        long offset = Long.parseLong(indexEntry[2]);
        return readBlockFromFile(filePath, offset);
    }

    private byte[] readBlockFromFile(String filePath, long offset) throws IOException, NoSuchAlgorithmException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(offset);
            Block block = Block.readFromFile(file, encryptor); // Read block from file
            return block.getSchema().isEncrypted() ? block.getData() : block.getData(); // Return decrypted or raw data
        }
    }

    private void updateIndex(String hash, String filePath, long offset) throws IOException {
        String indexFilePath = getIndexFilePath(hash);

        // Create the index file, parent directories if they don't exist
        File indexFile = new File(indexFilePath);
        indexFile.getParentFile().mkdirs(); // Ensure the parent directories exist

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile, true))) {
            writer.write(hash + "," + filePath + "," + offset);
            writer.newLine();
        }
    }

    private String[] findIndexEntry(String hash) throws IOException {
        String indexFilePath = getIndexFilePath(hash);
        File indexFile = new File(indexFilePath);

        // If the index file does not exist, return null
        if (!indexFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(hash)) return parts;
            }
        }
        return null;
    }

    private String getIndexFilePath(String hash) {
        // Use the same directory structure as block files for the index files
        return Paths.get(BLOCKS_DIR, hash.substring(0, 2), hash.substring(2, 4), hash.substring(4, 6), hash.substring(6,8) + ".idx").toString();
    }

    private String getBlockFilePath(String hash) {
        return Paths.get(BLOCKS_DIR, hash.substring(0, 2), hash.substring(2, 4), hash.substring(4, 6), hash.substring(6,8) + ".bfs").toString();
    }

    public boolean doesBlockExist(String hash) {
        String filePath = getBlockFilePath(hash); // Get the block file path
        File file = new File(filePath); // Create a File object
        return file.exists(); // Return true if the file exists, false otherwise
    }

    public static void main(String[] args) throws Exception {
        BlockStorage bfs = new BlockStorage(System.getProperty("user.dir"));
        String hash = "bbxxd1234ef56780";
        byte[] blockData = "Sample Block Data4443".getBytes();

        try {
            bfs.storeBlock(hash, blockData, false);
            byte[] retrievedData = bfs.readBlock(hash);
            System.out.println("Retrieved Block Data: " + new String(retrievedData));

            // Check if the block exists
            boolean exists = bfs.doesBlockExist(hash);
            System.out.println("Does Block Exist: " + exists);
        } catch (IOException e) {
            System.err.println("Error storing or reading block: " + e.getMessage());
        }
    }
}
