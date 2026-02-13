package com.myBackup.services.bfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class BlockStorageTest {
    private static final String TEST_HASH = "bbxxd1234ef56780";
    private static final String TEST_HASH_1 = "bbxxd1234ef56780";
    private static final String TEST_HASH_2 = "ccxxd1234ef56781";
    private static final String TEST_HASH_3 = "ddxxd1234ef56782";
    private static final byte[] TEST_BLOCK_DATA = "Sample Block Data4443".getBytes();
    private static final byte[] TEST_BLOCK_DATA_1 = "Sample Block Data 1".getBytes();
    private static final byte[] TEST_BLOCK_DATA_2 = "Sample Block Data 2".getBytes();
    private static final byte[] TEST_BLOCK_DATA_3 = "Sample Block Data 3".getBytes();

    @Test
    void testStoreAndReadBlock(@TempDir Path tempDir) throws IOException, NoSuchAlgorithmException {
        // Create a BlockStorage instance with the temporary directory
        BlockStorage blockStorage = new BlockStorage(tempDir.toString());

        // Store a block
        String storedHash = blockStorage.storeBlock(TEST_HASH, TEST_BLOCK_DATA, false);

        // Verify the stored hash is the same as expected
        assertEquals(TEST_HASH, storedHash, "Stored hash does not match expected hash.");

        // Read the block back
        byte[] retrievedData = blockStorage.readBlock(TEST_HASH);

        // Verify the retrieved data matches the original data
        assertArrayEquals(TEST_BLOCK_DATA, retrievedData, "Retrieved block data does not match the original data.");
    }

    @Test
    void testDoesBlockExist(@TempDir Path tempDir) throws IOException, NoSuchAlgorithmException {
        // Create a BlockStorage instance with the temporary directory
        BlockStorage blockStorage = new BlockStorage(tempDir.toString());

        // Store a block
        blockStorage.storeBlock(TEST_HASH, TEST_BLOCK_DATA, false);

        // Check if the block exists
        assertTrue(blockStorage.blockExists(TEST_HASH), "Block should exist after being stored.");

        // Construct the expected block file path manually
        String expectedBlockFilePath = tempDir.resolve("blocks")
                .resolve(TEST_HASH.substring(0, 2))
                .resolve(TEST_HASH.substring(2, 4))
                .resolve(TEST_HASH.substring(4, 6))
                .resolve(TEST_HASH.substring(6, 8) + ".bfs").toString();

        // Clean up the block by deleting the block file directly
        Files.deleteIfExists(Path.of(expectedBlockFilePath));

        // Verify that the block no longer exists
        assertFalse(blockStorage.blockExists(TEST_HASH), "Block should not exist after being deleted.");
    }

    @Test
    void testReadNonExistentBlock(@TempDir Path tempDir) {
        // Create a BlockStorage instance with the temporary directory
        BlockStorage blockStorage;
        try {
            blockStorage = new BlockStorage(tempDir.toString());
            // Attempt to read a block that does not exist
            assertThrows(FileNotFoundException.class, () -> blockStorage.readBlock("nonexistent_hash"),
                    "Expected FileNotFoundException when reading a nonexistent block.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testStoreBlockCreatesDirectoryStructure(@TempDir Path tempDir) throws IOException, NoSuchAlgorithmException {
        // Create a BlockStorage instance with the temporary directory
        BlockStorage blockStorage = new BlockStorage(tempDir.toString());

        // Store a block to ensure directory structure is created
        blockStorage.storeBlock(TEST_HASH, TEST_BLOCK_DATA, false);

        // Construct the expected block file path manually
        String expectedBlockFilePath = tempDir.resolve("blocks")
                .resolve(TEST_HASH.substring(0, 2))
                .resolve(TEST_HASH.substring(2, 4))
                .resolve(TEST_HASH.substring(4, 6))
                .resolve(TEST_HASH.substring(6, 8) + ".bfs").toString();

        // Check that the block file was created in the correct directory structure
        assertTrue(Files.exists(Path.of(expectedBlockFilePath)), "Block file should exist at expected path.");

        // Construct the expected index file path manually
        String expectedIndexFilePath = tempDir.resolve("blocks")
                .resolve(TEST_HASH.substring(0, 2))
                .resolve(TEST_HASH.substring(2, 4))
                .resolve(TEST_HASH.substring(4, 6))
                .resolve(TEST_HASH.substring(6, 8) + ".idx").toString();

        // Also check that the corresponding index file exists
        assertTrue(Files.exists(Path.of(expectedIndexFilePath)), "Index file should exist at expected path.");
    }

    @Test
    void testStoreAndReadMultipleBlocksInOneFile(@TempDir Path tempDir) throws IOException, NoSuchAlgorithmException {
        // Create a BlockStorage instance with the temporary directory
        BlockStorage blockStorage = new BlockStorage(tempDir.toString());

        // Store multiple blocks
        String storedHash1 = blockStorage.storeBlock(TEST_HASH_1, TEST_BLOCK_DATA_1, false);
        String storedHash2 = blockStorage.storeBlock(TEST_HASH_2, TEST_BLOCK_DATA_2, false);
        String storedHash3 = blockStorage.storeBlock(TEST_HASH_3, TEST_BLOCK_DATA_3, false);

        // Verify that the stored hashes are correct
        assertEquals(TEST_HASH_1, storedHash1, "Stored hash 1 does not match expected hash.");
        assertEquals(TEST_HASH_2, storedHash2, "Stored hash 2 does not match expected hash.");
        assertEquals(TEST_HASH_3, storedHash3, "Stored hash 3 does not match expected hash.");

        // Read the blocks back
        byte[] retrievedData1 = blockStorage.readBlock(TEST_HASH_1);
        byte[] retrievedData2 = blockStorage.readBlock(TEST_HASH_2);
        byte[] retrievedData3 = blockStorage.readBlock(TEST_HASH_3);

        // Verify that the retrieved data matches the original data
        assertArrayEquals(TEST_BLOCK_DATA_1, retrievedData1, "Retrieved block data 1 does not match original data.");
        assertArrayEquals(TEST_BLOCK_DATA_2, retrievedData2, "Retrieved block data 2 does not match original data.");
        assertArrayEquals(TEST_BLOCK_DATA_3, retrievedData3, "Retrieved block data 3 does not match original data.");

        // Construct expected block file path manually
        String expectedBlockFilePath = tempDir.resolve("blocks")
                .resolve(TEST_HASH_1.substring(0, 2))
                .resolve(TEST_HASH_1.substring(2, 4))
                .resolve(TEST_HASH_1.substring(4, 6))
                .resolve(TEST_HASH_1.substring(6, 8) + ".bfs").toString();

        // Verify that the block file exists
        assertTrue(Files.exists(Path.of(expectedBlockFilePath)), "Block file should exist at expected path.");

        // Verify that the index file exists
        String expectedIndexFilePath = tempDir.resolve("blocks")
                .resolve(TEST_HASH_1.substring(0, 2))
                .resolve(TEST_HASH_1.substring(2, 4))
                .resolve(TEST_HASH_1.substring(4, 6))
                .resolve(TEST_HASH_1.substring(6, 8) + ".idx").toString();
        
        assertTrue(Files.exists(Path.of(expectedIndexFilePath)), "Index file should exist at expected path.");
    }
}
