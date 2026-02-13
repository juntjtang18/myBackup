package com.myBackup.services.bfs;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BackupNode implements Serializable {
    private static final long serialVersionUID = 1L; // Unique identifier for serialization
    public enum NodeType {
        FILE,
        DIRECTORY
    }

    private BackupFile backupFile; // Reference to the associated BackupFile
    private List<BackupNode> children; // List of child nodes
    private BackupNode parent; // Reference to the parent node
    private NodeType nodeType; // Type of the node: FILE or DIRECTORY
    private String hash;
    
    public BackupNode(BackupFile backupFile, NodeType nodeType) {
        this.backupFile = backupFile;
        this.children = new ArrayList<>();
        this.parent = null; // No parent at creation
        this.nodeType = nodeType; // Set the node type
    }

    public BackupFile getBackupFile() {
        return backupFile;
    }

    public List<BackupNode> getChildren() {
        return children;
    }

    public BackupNode getParent() {
        return parent;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setParent(BackupNode parent) {
        this.parent = parent;
    }

    public void addChild(BackupNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void removeChild(BackupNode child) {
        this.children.remove(child);
    }

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getBackupFileName() {
	    return backupFile.getFileMeta().getFileName(); // Assuming BackupFile has a getName method
	}
	
    public String calculateHash() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); // or any preferred algorithm
        StringBuilder hashBuilder = new StringBuilder();

        if (this.nodeType == NodeType.FILE) {
            // For file nodes, concatenate block hashes from backupFile
            for (String blockHash : backupFile.getBlockMap()) {
                hashBuilder.append(blockHash);
            }
        } else { // Directory node
            // Sort children based on a specific attribute (e.g., name)
            Collections.sort(children, Comparator.comparing(node -> node.getBackupFileName()));

            // For directory nodes, concatenate children's hashes
            for (BackupNode child : children) {
                hashBuilder.append(child.calculateHash());
            }
        }

        // Calculate final hash
        byte[] hashBytes = digest.digest(hashBuilder.toString().getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString(); // Return the hex representation of the hash
    }
}
