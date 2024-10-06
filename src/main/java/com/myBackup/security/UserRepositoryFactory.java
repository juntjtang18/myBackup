package com.myBackup.security;

import com.myBackup.config.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRepositoryFactory {

    private final Config config;
    @Value("${user.repository.type:file}")
    private String repositoryType;

    public UserRepositoryFactory(Config config) {
        this.config = config;
    }

    @Bean
    public UserRepository userRepository() {
        if ("file".equalsIgnoreCase(repositoryType)) {
            return new UserRepositoryFileImpl(config);
        } 
        // In the future, we can add more types, e.g., database:
        // else if ("database".equalsIgnoreCase(repositoryType)) {
        //     return new UserRepositoryDatabaseImpl(config);
        // }
        throw new IllegalArgumentException("Unknown UserRepository type: " + repositoryType);
    }

}
