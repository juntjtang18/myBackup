package com.myBackup.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.myBackup.config.Config;

import org.springframework.security.core.userdetails.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final Config config;

    public UserDetailsServiceImpl(Config config) {
        this.config = config;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("UserDetailsServiceImpl::loadUserByUsername({})", username);
        Optional<UserDetails> userDetails = findUserInFile(username);

        return userDetails.orElseThrow(() -> 
            new UsernameNotFoundException("User not found: " + username));
    }

    private Optional<UserDetails> findUserInFile(String username) {
        String usersFilePath = config.getUsersFilePath();
        logger.info("UserDetailsServiceImpl::findUserInFile({}) - User File: {}", username, usersFilePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(usersFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 4 && parts[0].equals(username)) {
                    String storedUsername = parts[0];
                    String storedPassword = parts[1];
                    String roles = parts[2]; // Multiple roles
                    @SuppressWarnings("unused")
					String email = parts[3];

                    // Split roles and create a list of authorities
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                        //.map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                    	.map(role -> new SimpleGrantedAuthority(role.trim()))
                        .collect(Collectors.toList());
                    
                    logger.debug("authorities: {}", authorities);
                    
                    return Optional.of(new User(storedUsername, storedPassword, authorities));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read users file", e);
        }

        return Optional.empty();
    }
}
