package com.myBackup.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;


@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	@Autowired
	private UserDetailsService userDetailsService;
	
	
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository(); // Using default header name
    }
    
    @SuppressWarnings("removal")
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
        .and()
        .authorizeRequests(authorizeRequests ->
                authorizeRequests
                .requestMatchers("/", "/login", "/login-auto", "/logout", "/css/**", "/js/**", "/fonts/**", "/icons/**", "/images/**")
                .permitAll()
                .requestMatchers("/swagger-ui/**")
                .permitAll()
                .requestMatchers("/home").authenticated() // Ensure /home is authenticated
                .anyRequest().authenticated() // All other requests require authentication
            )
	        .csrf()
	        //.csrfTokenRepository(csrfTokenRepository())  // Ensure this is your custom repository
	        .and()
        	.formLogin().disable()
        	//.formLogin(form -> form
            //        .loginPage("/login")
            //        .successHandler((request, response, authentication) -> {
                        // Custom success logic here
            //            response.sendRedirect("/home");
            //        })
            //        .failureHandler(customAuthenticationFailureHandler()) // Ensure custom handler is used
            //        //.defaultSuccessUrl("/home", true)
            //        .failureUrl("/login?error=true")
            //        .permitAll()
            //)
            .logout()
	            .logoutUrl("/logout")
	            .logoutSuccessUrl("/login?logout=true")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	            .permitAll() // Ensure logout URL is accessible to all
	            .and()
            //.csrf().disable()
            .exceptionHandling()
            	.accessDeniedPage("/403");

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
    
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
    }

}
