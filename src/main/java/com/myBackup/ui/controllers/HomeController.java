package com.myBackup.ui.controllers;

import com.myBackup.client.services.UUIDService;
import com.myBackup.models.UserDto;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
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
                      HttpServletRequest request,  // Add HttpServletRequest parameter
                      HttpServletResponse response) throws IOException {
    	logger.info("Entering Post /login...");
    	
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Authenticated user: {}", authentication.getName());
            logger.info("Authorities: {}", authentication.getAuthorities());
            HttpSession session = request.getSession(true);  // create if not exists
            logger.info("Session ID: {}", session.getId());
            request.changeSessionId();  // This regenerates the session ID.
            HttpSession newSession = request.getSession();  // Gets the new session.
            newSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            response.addCookie(new Cookie("JSESSIONID", session.getId()));
            /*
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
            }
            logger.info("csrfToken: {}", csrfToken.getToken());
            */
            response.sendRedirect("/home");
        } catch (AuthenticationException e) {
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
