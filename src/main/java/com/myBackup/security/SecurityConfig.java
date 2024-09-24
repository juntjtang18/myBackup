package com.myBackup.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/", "/home","/css/**", "/js/**", "/fonts/**", "/icons/**", "/images/**").permitAll() // Allow access to these paths
                    .anyRequest().permitAll() // All other requests require authentication
            )
            .formLogin(formLogin -> 
                formLogin
                    .loginPage("/login") // Customize login page if needed
                    .permitAll() // Allow everyone to see the login page
            )
            .logout(logout ->
                logout
                    .permitAll() // Allow everyone to log out
            )
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity (only if not needed)
            ; // Enable Basic Auth for testing (optional)

        return http.build();
    }
}
