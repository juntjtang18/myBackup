package com.myBackup.services.bfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TreeManagerTest {
    private TreeManager treeManager;
    private BackupNode originalRoot;

    @BeforeEach
    void setUp() throws IOException {
        // Set up the tree manager with a sample tree root directory
        String treeRoot = "D:\\develop\\hello-security"; // Change to your directory
        treeManager = new TreeManager(treeRoot);
        originalRoot = createSampleTree(); // Create a sample tree for testing
    }

    @Test
    void testSaveAndReadBackupTree() throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        // Save the backup tree
        treeManager.saveBackupTree(originalRoot);

        // Read the backup tree
        BackupNode restoredRoot = treeManager.readBackupTree(originalRoot.calculateHash());

        // Verify the structure and content of the original and restored trees
        assertTrue(areTreesEqual(originalRoot, restoredRoot), "The restored tree does not match the original tree.");
    }

    private BackupNode createSampleTree() {
        // Create a sample tree structure
        FileMeta fileMeta1 = new FileMeta("file1.txt", 100, "2024-10-01");
        Set<String> blockHashes1 = new HashSet<>();
        blockHashes1.add("block1hash");
        BackupFile backupFile1 = new BackupFile(fileMeta1, blockHashes1);
        
        BackupNode fileNode1 = new BackupNode(backupFile1, BackupNode.NodeType.FILE);
        
        FileMeta fileMeta2 = new FileMeta("file2.txt", 200, "2024-10-02");
        Set<String> blockHashes2 = new HashSet<>();
        blockHashes2.add("block2hash");
        BackupFile backupFile2 = new BackupFile(fileMeta2, blockHashes2);
        
        BackupNode fileNode2 = new BackupNode(backupFile2, BackupNode.NodeType.FILE);

        // Create a directory node
        BackupNode dirNode = new BackupNode(null, BackupNode.NodeType.DIRECTORY);
        dirNode.addChild(fileNode1);
        dirNode.addChild(fileNode2);

        return dirNode; // Return the root of the sample tree
    }

    private boolean areTreesEqual(BackupNode node1, BackupNode node2) {
        // Check if both nodes are null
        if (node1 == null && node2 == null) return true;
        // Check if either is null
        if (node1 == null || node2 == null) return false;

        // Compare BackupFile instances
        if (!node1.getBackupFile().equals(node2.getBackupFile())) return false;

        // Compare children
        if (node1.getChildren().size() != node2.getChildren().size()) return false;

        for (int i = 0; i < node1.getChildren().size(); i++) {
            if (!areTreesEqual(node1.getChildren().get(i), node2.getChildren().get(i))) {
                return false; // Found a mismatch
            }
        }

        return true; // All checks passed, trees are equal
    }
}

