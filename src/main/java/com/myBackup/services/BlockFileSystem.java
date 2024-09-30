package com.myBackup.services;

import com.myBackup.services.BlockStorage.Block;
import com.myBackup.services.BlockStorage.Encryptor;

import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;

public class BlockFileSystem {
    private static final String BLOCKS_DIR = "blocks";
    private static final String INDEX_DIR = Paths.get("blocks","index").toString();
    private final Encryptor encryptor;

    public BlockFileSystem(Encryptor encryptor) {
        this.encryptor = encryptor;
        initializeDirectoryStructure();
    }

    private void initializeDirectoryStructure() {
        try {
            // Create the base blocks directory first
            Path blocksPath = Paths.get(BLOCKS_DIR);
            Files.createDirectories(blocksPath);

            // Create the index directory under the blocks directory
            Path indexPath = Paths.get(BLOCKS_DIR, "index"); // INDEX_DIR is now relative to BLOCKS_DIR
            Files.createDirectories(indexPath);

            System.out.println("Directories created successfully.");
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }

    public void storeBlock(String hash, byte[] blockData, boolean encrypt) throws IOException, NoSuchAlgorithmException {
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
            block.writeTo(raf); // Write the block to the file at the current pointer position
            updateIndex(hash, filePath, offset); // Update index with the file path and offset
        }
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INDEX_DIR + "/index.bfs", true))) {
            writer.write(hash + "," + filePath + "," + offset);
            writer.newLine();
        }
    }

    private String[] findIndexEntry(String hash) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(INDEX_DIR + "/index.bfs"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(hash)) return parts;
            }
        }
        return null;
    }

    private String getBlockFilePath(String hash) {
        return BLOCKS_DIR + "/" + hash.substring(0, 2) + "/" + hash.substring(2, 4) + ".bfs";
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        BlockFileSystem bfs = new BlockFileSystem(null);
        String hash = "bbxxd1234ef56780";
        byte[] blockData = "Sample Block Data4443".getBytes();

        try {
            bfs.storeBlock(hash, blockData, false);
            byte[] retrievedData = bfs.readBlock(hash);
            System.out.println("Retrieved Block Data: " + new String(retrievedData));
        } catch (IOException e) {
            System.err.println("Error storing or reading block: " + e.getMessage());
        }
    }
}
