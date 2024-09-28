package com.myBackup.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryFactory userRepositoryFactory, PasswordEncoder passwordEncoder, 
                       @Value("${user.repository.type:file}") String repositoryType) {
        this.userRepository = userRepositoryFactory.createUserRepository(repositoryType);
        this.passwordEncoder = passwordEncoder;
    }

    // Load a user by username (Spring Security UserDetailsService integration)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username: {}", username);
        try {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isEmpty()) {
                logger.warn("User not found with username: {}", username);
                throw new UsernameNotFoundException("User not found: " + username);
            }
            logger.debug("User found: {}", username);
            User user = optionalUser.get();
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getEncryptedPassword(),
                    AuthorityUtils.createAuthorityList(user.getRole()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users", e);
        }
    }

    // Check if the registered user is the first one in the system
    public boolean isFirstUser() throws IOException {
        List<User> allUsers = userRepository.loadAllUsers();
        return allUsers.isEmpty(); // If no users exist, the new one will be the first
    }

    // Register a new user
    public void registerUser(String username, String password, String email) throws IOException {
        logger.debug("Registering user: {} with email: {}", username, email);

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        // Determine the role for the new user
        String role = isFirstUser() ? Role.ROLE_ADMIN.getRoleName() : Role.ROLE_USER.getRoleName();

        // Encrypt the password
        String encryptedPassword = passwordEncoder.encode(password);

        // Create and save the new user
        User user = new User(username, encryptedPassword, role, email);
        userRepository.save(user);

        logger.debug("User registered successfully: {}", username);
    }
}
