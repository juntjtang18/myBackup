package com.myBackup.services.bfs;

// Represents the entire BackupTree
public class BackupTree {
    private BackupNode root; // The root node of the tree

    public BackupTree(BackupNode root) {
        this.root = root;
    }

    public BackupNode getRoot() {
        return root;
    }

}
