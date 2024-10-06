package com.myBackup.services.bfs;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.myBackup.services.HashManager;


/*
 * This class access the backup files in a repository.
 * given a repositoryID -> repository directory, it will read/write the backup file
 * 
 */
@Service
/*
 * 
 * <repo_root>/backups/
				└── john_doe/
				    ├── 00-14-22-01-23-45/
				    │   └── mydoc_f_doc_x_slides/
				    │       └── xyz789hash.json
				    └── another_source_directory/
    
    
 */
public class BackupStorage {
	private String repoID;
	private String repoRoot;
	
	
    public static List<Backup> listBackupsForSourceDirectory(String user, String clientID, String sourceDir) {
        List<Backup> backups = loadBackupsFromFile(user, clientID, sourceDir);
        Collections.sort(backups, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return backups;
    }

    public static void addBackup(String user, String clientID, String sourceDir, Backup backup) {
        String filePath = generateBackupHistoryFilePath(user, clientID, sourceDir);
        List<Backup> backups = loadBackupsFromFile(user, clientID, sourceDir);
        backups.add(backup);
        saveBackupsToFile(filePath, backups);
    }

    private static List<Backup> loadBackupsFromFile(String user, String clientID, String sourceDir) {
        // Load and deserialize from the given filePath
        return null;
    }

    private static void saveBackupsToFile(String filePath, List<Backup> backups) {
        // Serialize and save to the given filePath
    }

    private static String sanitizeFileName(String sourceDir) {
        // Replace special characters with '_'
        String sanitized = sourceDir.replaceAll("[^a-zA-Z0-9/._]", "_");

        // Skip the first character if it's '_'
        if (sanitized.startsWith("_")) {
            sanitized = sanitized.substring(1);
        }

        if (sanitized.length() <= 12) {
            return sanitized;
        } else {
            // Get the first character of the original sanitized string
            String firstChar = sanitized.charAt(0) + "";

            // Get the last 8 characters of the sanitized string
            String lastPart = sanitized.length() > 8 ? sanitized.substring(sanitized.length() - 8) : sanitized;

            return firstChar + "..." + lastPart; // Combine '...', last 8 chars, and the first character
        }
    }

    private static String generateBackupHistoryFilePath(String user, String clientID, String sourceDir) {
        String sanitizedFileName = sanitizeFileName(sourceDir);
        String hash = HashManager.generateUniqueHash(user+clientID+sourceDir+sanitizedFileName);
        
        // Determine the file separator based on the OS
        String fileSeparator = File.separator;

        return String.format("backups%s%s%s%s%s%sbackuphistory_%s.json",
                fileSeparator, user, fileSeparator, clientID, fileSeparator, sanitizedFileName, hash);
    }
    
    public BackupStorage buildStorage() {
    	return new BackupStorage();
    }

}
