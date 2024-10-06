package com.myBackup.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository; // this userRepository will be injected from UserRepositoryFactory.createUserRepository(...)
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Injecting PasswordEncoder

    public User loadUserByUsername(String username) throws UserNotFoundException {
        logger.debug("Attempting to load user by username: {}", username);
        
        try {
            // Use Optional's ifPresentOrElse method for cleaner handling
            return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new UserNotFoundException("User not found: " + username);
                });
        } catch (IOException e) {
            logger.error("IOException occurred while loading user: {}", username, e);
            throw new UserNotFoundException("Failed to load user due to an internal error: " + username);
        }
    }

    public boolean isFirstUser() throws IOException {
        return userRepository.loadAllUsers().isEmpty(); // If no users exist, the new one will be the first
    }

    public void registerUser(String username, String password, String email) throws IOException {
        logger.debug("Registering user: {} with email: {}", username, email);

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        String role = isFirstUser() ? Role.ROLE_ADMIN.getRoleName() : Role.ROLE_USER.getRoleName();
        
        // Encode the password before saving
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(username, hashedPassword, role, email);
        userRepository.save(user);

        logger.debug("User registered successfully: {}", username);
    }

    public void updateUserRole(String username, String newRole) throws IOException {
        logger.debug("Updating role for user: {}", username);
        
        User user = loadUserByUsername(username);
        user.setRole(newRole);
        
        userRepository.updateUser(user); 
        logger.debug("Role updated successfully for user: {}", username);
    }
}
