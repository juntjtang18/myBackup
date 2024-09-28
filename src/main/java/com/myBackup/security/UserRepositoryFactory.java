package com.myBackup.security;

import com.myBackup.config.Config;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryFactory {

    private final Config config;

    public UserRepositoryFactory(Config config) {
        this.config = config;
    }

    // Factory method to create the appropriate UserRepository implementation
    public UserRepository createUserRepository(String type) {
        if ("file".equalsIgnoreCase(type)) {
            return new UserRepositoryFileImpl(config);  // File-based user repository
        } 
        // In the future, you can add more types, e.g., database:
        // else if ("database".equalsIgnoreCase(type)) {
        //     return new UserRepositoryDatabaseImpl(config);
        // }
        throw new IllegalArgumentException("Unknown UserRepository type: " + type);
    }
}
