package com.myBackup.ui.controllers;

import com.myBackup.client.services.UUIDService;
import com.myBackup.models.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
	
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private UUIDService uuidService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/")
    public String originalStart(HttpServletRequest request, Model model) {
        String remoteAddr = request.getRemoteAddr();
        logger.info("Request from {} received. ", remoteAddr);
        
        UserDto user = new UserDto();
        user.setUsername(System.getProperty("user.name"));
        user.setPassword(uuidService.getUUID());              
        model.addAttribute("user", user);  

        return "login";  // This template should handle user input separately
    }

    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        UserDto user = new UserDto();
        user.setUsername(System.getProperty("user.name"));
        user.setPassword(uuidService.getUUID());              
        model.addAttribute("user", user);  
        return "login";
    }
    
    @PostMapping("/login")
    public void login(@RequestParam String username, 
                      @RequestParam String password, 
                      HttpServletResponse response) throws IOException {
        try {
            // Create an authentication token with the provided credentials
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            
            // If authentication is successful, redirect to /home
            response.sendRedirect("/home");

        } catch (AuthenticationException e) {
            // If authentication fails, return 401 Unauthorized status
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login failed: " + e.getMessage());
        }
    }


    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            // Perform logout by invalidating the user's session
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return ResponseEntity.ok("User logged out successfully");
    }    
    // Utility method to check if the request is local
    @SuppressWarnings("unused")
	private boolean isLocalRequest(String remoteAddr) {
        return "127.0.0.1".equals(remoteAddr) || "localhost".equals(remoteAddr) || "::1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr);
    }
    
    @GetMapping("/home")
    public String home(HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                model.addAttribute("username", username);
            } else {
                model.addAttribute("username", principal.toString());
            }
        }
        
        String clientID = uuidService.getUUID();
        model.addAttribute("clientID", clientID);
        return "home";
    }
    
    @GetMapping("/start")
    public String start() {
    	return "dashboard";
    }
    
    @GetMapping("/testapi")
    public String testapi() {
    	return "testapi";
    }
    
    @GetMapping("/adminhome")
    public String adminHome() {
    	return "admin/adminhome.html";
    }
}
