package com.myBackup.client.services;

import com.myBackup.client.Utility;
import com.myBackup.security.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AutoLoginFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AutoLoginFilter.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AutoLoginFilter(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Log the request details
        logRequestDetails(httpRequest);

        logger.debug("AutoLogin filter triggered. Current authentication: {}", authentication);
        
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.equals("/login") || (authentication != null && !(authentication instanceof AnonymousAuthenticationToken))) {
            // If the request is for the login page or the user is authenticated, continue the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Check if the user is already authenticated
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            // User is already authenticated; continue the filter chain
            chain.doFilter(request, response);
            return;
        }
        
        logger.debug("Current authentication is null or anonymouse. {}", authentication);

        // Check if the request is coming from localhost
        if (isLocalhost(httpRequest.getRemoteAddr())) {
            String username = System.getProperty("user.name");
            String macAddress = Utility.getMACAddress(); // Retrieve the MAC address

            logger.debug("Request from localhost, attempting auto-login with user: {}", username);
            // Try to authenticate the user
            try {
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, macAddress);
                Authentication authentication2 = authenticationManager.authenticate(authRequest);
                SecurityContextHolder.getContext().setAuthentication(authentication2);
                
                logger.debug("AutoLogin success: {}", SecurityContextHolder.getContext().getAuthentication());

                // Redirect to /home after successful login

            } catch (Exception e) {
                logger.error("AutoLogin failed: {}", e.getMessage(), e);
                // Redirect to login if authentication fails
                //httpResponse.sendRedirect("/login");
            }
        }

        chain.doFilter(request, response); // Continue with the filter chain
    }

    private boolean isLocalhost(String remoteAddr) {
        return "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr); // Check for IPv4 and IPv6 localhost
    }

    // Method to log request details
    private void logRequestDetails(HttpServletRequest request) {
        logger.debug("Request URI: http://{} {} {}", request.getRemoteAddr(), request.getMethod(), request.getRequestURI());

        // Log headers
        //request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
        //    logger.debug("Header: {} = {}", headerName, request.getHeader(headerName))
        //);

        // Log query parameters
        //request.getParameterMap().forEach((key, value) -> 
        //    logger.debug("Query Parameter: {} = {}", key, String.join(", ", value))
        //);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Optional: initialization code here
    }

    @Override
    public void destroy() {
        // Optional: cleanup code here
    }
}
