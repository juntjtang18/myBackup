package com.myBackup.services.bfs;

import java.util.ArrayList;
import java.util.List;

public class BackupNode {
    public enum NodeType {
        FILE,
        DIRECTORY
    }

    private BackupFile backupFile; // Reference to the associated BackupFile
    private List<BackupNode> children; // List of child nodes
    private BackupNode parent; // Reference to the parent node
    private NodeType nodeType; // Type of the node: FILE or DIRECTORY

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

    // Additional methods for tree manipulation can be added here
}
