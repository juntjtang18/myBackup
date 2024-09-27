package com.myBackup.config;

import org.ini4j.Ini;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config instance;

    private String jobsDirectory;
    @SuppressWarnings("unused")
	private String usersFilePath;
    private boolean encryptData;
    private String backupRepositoryPath;
	private int threadPoolSize;

    // Private constructor to prevent instantiation
    private Config() {
        // Load configurations during initialization
        try {
            String iniFilePath = getIniFilePath();
            logger.debug("Loading configuration from: " + iniFilePath);
            loadConfigurations(iniFilePath);
            logger.debug("File Paths: usersFilePath: {}   jobsDirectory: {}  backupRepositoryPath: {} ", usersFilePath, jobsDirectory, backupRepositoryPath);
        } catch (IOException e) {
            logger.debug("Failed to load configuration", e);
        }
    }

    // Provide a global point of access to the instance
    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    private String getIniFilePath() {
        // Check the ENV variable and set the ini file path accordingly
        String env = System.getenv("ENV");
        logger.debug(" getIniFilePath() --- System.getenv('ENV')={}", env);
        
        if ("JUNIT".equalsIgnoreCase(env)) {
            return "D:\\develop\\myBackup\\src\\main\\resources\\myBackup.ini";
        }
        // Default to the app directory
        return getAppDirectory() + File.separator + "myBackup.ini";
    }

    public void loadConfigurations(String iniFilePath) throws IOException {
        Ini ini;

        // Load the file from the filesystem
        File iniFile = new File(iniFilePath);
        if (!iniFile.exists()) {
            throw new IOException("Configuration file not found: " + iniFilePath);
        }

        try (InputStream inputStream = new FileInputStream(iniFile)) {
            ini = new Ini(inputStream);
        } catch (IOException e) {
            logger.error("Failed to load INI file: " + iniFilePath, e);
            throw e; // Re-throw the exception after logging
        }

        // Retrieve and set values from the 'Paths' and 'settings' sections
        String backupRepoPathFromIni = ini.get("Paths", "backupRepositoryPath");
        this.backupRepositoryPath = Optional.ofNullable(backupRepoPathFromIni)
            .filter(path -> !path.isEmpty())
            .map(path -> new File(getAppDirectory(), path).getAbsolutePath())
            .orElse(new File(getAppDirectory(), "backupRepository").getAbsolutePath());

        // Ensure backup repository directory exists
        File backupRepoDir = new File(backupRepositoryPath);
        if (!backupRepoDir.exists()) {
            if (backupRepoDir.mkdirs()) {
                logger.info("Created backup repository directory: " + backupRepositoryPath);
            } else {
                logger.error("Failed to create backup repository directory: " + backupRepositoryPath);
            }
        }

        // Set jobsDirectory and ensure it exists
        this.jobsDirectory = new File(backupRepositoryPath, "jobs").getAbsolutePath();
        File jobsDir = new File(jobsDirectory);
        if (!jobsDir.exists()) {
            if (jobsDir.mkdirs()) {
                logger.info("Created jobs directory: " + jobsDirectory);
            } else {
                logger.error("Failed to create jobs directory: " + jobsDirectory);
            }
        }

        // Set usersFilePath
        this.usersFilePath = new File(backupRepositoryPath, "users.pwd").getAbsolutePath();
        this.encryptData = Boolean.parseBoolean(Optional.ofNullable(ini.get("settings", "encrypt_data")).orElse("false"));
        this.threadPoolSize = Integer.parseInt(Optional.ofNullable(ini.get("settings", "threadPoolSize")).orElse("5"));
    }

    public String getAppDirectory() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String jarPath = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            File jarDir;
            
            logger.debug("getAppDirectory() --- os={}  jarPath={} ", os, jarPath);
            
            if (os.contains("win")) {
                jarDir = new File(jarPath).getParentFile();
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                jarDir = new File(jarPath).getParentFile().getParentFile().getParentFile();
            } else {
                throw new IllegalStateException("Unsupported OS");
            }
            logger.debug("Config::getAppDirectory() --- jarDir={}", jarDir);
            
            // Check if the system property 'appDir' is set
            String appDir = System.getProperty("appDir");
            
            logger.debug(" System.getProperty(appDir)={}", appDir);
            
            if (appDir != null) {
                return appDir;
            }

            // Default to jar directory if 'appDir' is not set
            return jarDir.getAbsolutePath();
            
        } catch (URISyntaxException e) {
            logger.error("Failed to get app directory", e);
            throw new RuntimeException("Failed to get app directory", e);
        }
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    public String getJobsDirectory() {
        return jobsDirectory;
    }

    public String getUsersFilePath() {
        //return Paths.get(System.getProperty("user.dir"), "sys", "user.pwd").toString();
    	return usersFilePath;
    }

    public boolean isEncryptData() {
        return encryptData;
    }

    public String getBackupRepositoryPath() {
        return backupRepositoryPath;
    }
}
