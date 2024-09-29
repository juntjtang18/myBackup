package com.myBackup.server.repository;

import java.io.File;

public class RepositoryInitializer {

    private final String baseDir;
    private final String user;
    private final String computerId;

    public RepositoryInitializer(String baseDir, String user, String computerId) {
        this.baseDir = baseDir;
        this.user = user;
        this.computerId = computerId;
    }

    public void prepareDirectoryStructure(String relativePath) {
        String destDir = String.format("%s/%s/%s/%s", baseDir, user, computerId, relativePath);
        createDirectory(destDir);

        // Create subdirectories
        createDirectory(destDir + "/trees");
        createDirectory(destDir + "/refs");
        createDirectory(destDir + "/source_mapping");

        // Create objects directory
        createDirectory(baseDir + "/objects");
    }

    // Check if the repository exists by verifying essential directories
    public boolean exists() {
        String userDir = String.format("%s/%s/%s", baseDir, user, computerId);
        File treesDir = new File(userDir + "/trees");
        File refsDir = new File(userDir + "/refs");
        File sourceMappingDir = new File(userDir + "/source_mapping");
        File objectsDir = new File(baseDir + "/objects");

        return treesDir.exists() && refsDir.exists() && sourceMappingDir.exists() && objectsDir.exists();
    }
    
    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Created directory: " + path);
            } else {
                System.err.println("Failed to create directory: " + path);
            }
        } else {
            System.out.println("Directory already exists: " + path);
        }
    }
}
