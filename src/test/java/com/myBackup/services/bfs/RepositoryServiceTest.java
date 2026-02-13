package com.myBackup.services.bfs;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryServiceTest {

    private Repository repository;
    private FileRefManager fileRefManager;
    private BlockStorage blockStorage;
    private RepositoryService repositoryService;

    @TempDir
    Path tempDirectory; // Automatically handled by JUnit

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize the actual implementations
        String repositoryRoot = tempDirectory.toString();
        fileRefManager = new FileRefManager(repositoryRoot);
        blockStorage = new BlockStorage(repositoryRoot); // Assuming BlockStorage constructor takes a root directory

        repository = new Repository(); // Initialize the Repository class as needed
        repositoryService = new RepositoryService(repository, fileRefManager, blockStorage);
    }

    @Test
    public void testCommitAndReadBackupFile() throws IOException, NoSuchAlgorithmException {
        // Given: A BackupFile with a set of blocks
        String backupFileHash = "abc123456789defabcd5678abcdef12";
        Set<String> blockHashes = new HashSet<>();
        blockHashes.add("blockHash1");
        blockHashes.add("blockHash2");
        blockHashes.add("blockHash3");
        BackupFile backupFile = new BackupFile(new FileMeta("path/to/file.txt", 0, "2024-10-06"), blockHashes);

        // Prepare block data
        byte[] blockData1 = new byte[]{1}; // Data for blockHash1
        byte[] blockData2 = new byte[]{2}; // Data for blockHash2
        byte[] blockData3 = new byte[]{3}; // Data for blockHash3

        // When: Uploading blocks using RepositoryService
        repositoryService.uploadBlock("blockHash1", blockData1, false);
        repositoryService.uploadBlock("blockHash2", blockData2, false);
        repositoryService.uploadBlock("blockHash3", blockData3, false);

        // Commit the file
        repositoryService.commitAFile(backupFileHash, backupFile);

        // Then: Retrieve the BackupFile
        Optional<BackupFile> retrievedBackupFileOpt = repositoryService.getBackupFile(backupFileHash);
        assertTrue(retrievedBackupFileOpt.isPresent(), "The BackupFile should be retrieved successfully.");
        
        BackupFile retrievedBackupFile = retrievedBackupFileOpt.get();

        // Verify the block hashes in the retrieved file
        assertEquals(blockHashes, retrievedBackupFile.getBlockMap(), "The block maps should match.");

        // Read the blocks back through the RepositoryService and verify their content
        for (String blockHash : retrievedBackupFile.getBlockMap()) {
            byte[] blockData = repositoryService.readBlock(blockHash, false);
            assertNotNull(blockData, "The block data should not be null.");
            assertTrue(blockData.length > 0, "The block data should have content.");
            
            // Verify the content of the block data
            switch (blockHash) {
                case "blockHash1":
                    assertArrayEquals(new byte[]{1}, blockData, "Block data for blockHash1 should match.");
                    break;
                case "blockHash2":
                    assertArrayEquals(new byte[]{2}, blockData, "Block data for blockHash2 should match.");
                    break;
                case "blockHash3":
                    assertArrayEquals(new byte[]{3}, blockData, "Block data for blockHash3 should match.");
                    break;
                default:
                    fail("Unexpected block hash: " + blockHash);
            }
        }
    }
}
