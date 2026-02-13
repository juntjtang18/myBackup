package com.myBackup.services.bfs;

import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.Stack;

public class TreeManager {
    private final String treeRoot;

    public TreeManager(String treeRoot) {
        this.treeRoot = treeRoot;
    }

    // Save a tree into <tree_root>/<hash2char>/<hash2char>/hash2char/<hash2char>/hashrestchar.tree
    public void saveBackupTree(BackupNode root) throws IOException, NoSuchAlgorithmException {
        String hash = root.calculateHash();
        root.setHash(hash); // Set the hash to the root node
        String filePath = buildFilePath(hash);
        
        // Ensure the directory exists
        Files.createDirectories(Paths.get(filePath).getParent());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            // Perform a depth-first traversal to save the nodes
            traverseAndSave(root, oos);
        }
    }

    // Traverse the tree and save each node in order
    private void traverseAndSave(BackupNode node, ObjectOutputStream oos) throws IOException {
        oos.writeObject(node); // Write the current node

        // If the node is a directory, traverse its children
        if (node.getNodeType() == BackupNode.NodeType.DIRECTORY) {
            for (BackupNode child : node.getChildren()) {
                traverseAndSave(child, oos); // Recursively save each child
            }
        }
    }

 // Read the tree file into a BackupNode
    public BackupNode readBackupTree(String hash) throws IOException, ClassNotFoundException {
        String filePath = buildFilePath(hash);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            // Read all nodes from the file and reconstruct the tree
            return reconstructTree(ois);
        }
    }

 // Reconstruct the tree from the ObjectInputStream
    private BackupNode reconstructTree(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Stack<BackupNode> nodeStack = new Stack<>();
        
        // Read nodes in a loop until EOF
        while (true) {
            try {
                BackupNode node = (BackupNode) ois.readObject(); // Read each node
                
                // If the node is a directory, check if we need to add children
                if (node.getNodeType() == BackupNode.NodeType.DIRECTORY) {
                    // If there's a parent node, add this node as a child
                    if (!nodeStack.isEmpty()) {
                        BackupNode parent = nodeStack.peek(); // Look at the top without removing it
                        parent.addChild(node);
                    }
                }

                // Add the current node to the stack
                nodeStack.push(node);
            } catch (EOFException e) {
                break; // End of file reached
            }
        }
        
        // Now we need to build the parent-child relationships
        BackupNode root = null;
        while (!nodeStack.isEmpty()) {
            BackupNode node = nodeStack.pop(); // Get the last added node

            if (node.getNodeType() == BackupNode.NodeType.DIRECTORY && root == null) {
                root = node; // The first directory read is considered as root
            } else if (node.getParent() != null) {
                // If node has a parent, link them accordingly
                BackupNode parent = node.getParent();
                parent.addChild(node);
            }
        }
        
        return root; // Return the reconstructed tree
    }


    // Check if the hash exists
    public boolean hashExists(String hash) {
        String filePath = buildFilePath(hash);
        return Files.exists(Paths.get(filePath));
    }

    // Delete the tree file if it exists
    public boolean deleteBackupTree(String hash) throws IOException {
        String filePath = buildFilePath(hash);
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            return true; // Deleted
        }
        return false; // Not found
    }

    private String buildFilePath(String hash) {
        return Paths.get(treeRoot, 
                         hash.substring(0, 2), 
                         hash.substring(2, 4), 
                         hash.substring(4, 6), 
                         hash.substring(6, 8), 
                         hash.substring(8) + ".tree").toString();
    }
}
