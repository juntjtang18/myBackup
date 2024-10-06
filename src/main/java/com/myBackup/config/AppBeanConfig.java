package com.myBackup.config;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.myBackup.security.KeyStoreManager;
import com.myBackup.services.bfs.EncryptorAES;

@Configuration

public class AppBeanConfig {
    private final KeyStoreManager keyStoreManager;

    public AppBeanConfig(KeyStoreManager keyStoreManager) {
        this.keyStoreManager = keyStoreManager; // Inject KeyStoreManager
    }
    
    @Bean
    public SecretKey secretKey() throws Exception {
        return keyStoreManager.loadOrCreateSecretKey(); // Create SecretKey bean
    }

    @Bean
    public EncryptorAES encryptorAES(SecretKey secretKey) {
        return new EncryptorAES(secretKey); // Create EncryptorAES bean with injected SecretKey
    }
}
