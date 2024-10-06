package com.myBackup.config;

import org.ini4j.Ini;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
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
@Lazy
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config instance;

    //private String jobsDirectory;
    private String usersFilePath;
    private boolean encryptData;
    private String repositoryFilePath;
	private String metaDirectory;			// the directory storing the meta data files. critical
	private String serversFilePath;			// the file records the servers, including localhost:8080
	private int    threadPoolSize;
	private String uuidFilePath;
	private String jobsFilePath;
	private String hashCollisionFile;
	private String keyStoreFilePath;
	private String keyStorePassword;
	private String keyPassword;
	private String keyAlias;
	
    // Private constructor to prevent instantiation
    private Config() {
        // Load configurations during initialization
        try {
            String iniFilePath = getIniFilePath();
            logger.debug("Loading configuration from: " + iniFilePath);
            loadConfigurations(iniFilePath);
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

        this.metaDirectory 		= Paths.get(getAppDirectory(),"meta").toString();
        this.usersFilePath 		= new File(metaDirectory, "users.pwd").getAbsolutePath();
        this.repositoryFilePath = Paths.get(metaDirectory, "repositories.json").toString();
        this.jobsFilePath		= Paths.get(metaDirectory, "jobs.json").toString();
        this.serversFilePath 	= Paths.get(metaDirectory,"servers.json").toString();
        this.hashCollisionFile	= Paths.get(metaDirectory, "hashcollision.txt").toString();
        this.uuidFilePath 		= Paths.get(getAppDirectory(), "config", "uuid.id").toString();
        
        this.encryptData = Boolean.parseBoolean(Optional.ofNullable(ini.get("settings", "encrypt_data")).orElse("false"));
        this.threadPoolSize = Integer.parseInt(Optional.ofNullable(ini.get("settings", "threadPoolSize")).orElse("5"));

        
        this.keyStoreFilePath = Paths.get(getAppDirectory(),"config","keystore.ks").toString();
        this.keyStorePassword = Optional.ofNullable(ini.get("key",  "keystorefile_pwd")).orElse("myBackup");
        this.keyPassword      = Optional.ofNullable(ini.get("key", "keystore_pwd")).orElse("myBackup");
        this.keyAlias         = Optional.ofNullable(ini.get("key",  "alias")).orElse("myBackup");
        
        logger.info("File Paths: metaDirector: {} usersFilePath: {}  backupRepositoryFilePath: {}   serversFilePath: {}    uuidFilePath: {}", 
        		                 metaDirectory, usersFilePath, repositoryFilePath, serversFilePath, uuidFilePath);
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
            //logger.debug("Config::getAppDirectory() --- jarDir={}", jarDir);
            
            // Check if the system property 'appDir' is set
            String appDir = System.getProperty("appDir");
            
            //logger.debug(" System.getProperty(appDir)={}", appDir);
            
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
    
    //public String getJobsDirectory() {
    //    return jobsDirectory;
    //}

    public String getUsersFilePath() {
    	return usersFilePath;
    }

    public boolean isEncryptData() {
        return encryptData;
    }

    public String getBackupRepositoryFilePath() {
        return repositoryFilePath;
    }

	public String getMetaDirectory() {
		return metaDirectory;
	}

	public String getServersFilePath() {
		return serversFilePath;
	}

	public String getUuidFilePath() {
		return uuidFilePath;
	}

	public String getJobFilePath() {
		return jobsFilePath;
	}

	public String getHashCollisionFile() {
		return hashCollisionFile;
	}

	public void setHashCollisionFile(String hashCollisionFile) {
		this.hashCollisionFile = hashCollisionFile;
	}

	public String getKeyStoreFilePath() {
		return keyStoreFilePath;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	//public void setJobsFilePath(String jobsFilePath) {
	//	this.jobsFilePath = jobsFilePath;
	//}

}
