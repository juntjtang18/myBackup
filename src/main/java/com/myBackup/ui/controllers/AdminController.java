package com.myBackup.ui.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myBackup.models.UserDto;
import com.myBackup.security.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminController {
    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDto") UserDto userDto, 
                               Model model, 
                               RedirectAttributes redirectAttributes, 
                               HttpServletRequest request) {
        try {
            // Check if the username already exists
            UserDetails existingUser = null;
            try {
                existingUser = userService.loadUserByUsername(userDto.getUsername());
            } catch (UsernameNotFoundException e) {
                // User does not exist, continue with registration
            }

            if (existingUser != null) {
                model.addAttribute("error", "Username already exists.");
                return "register";
            }

            // Register the new user
            userService.registerUser(userDto.getUsername(), userDto.getPassword(), userDto.getEmail());

            // Add username and password to redirect attributes for form submission
            redirectAttributes.addFlashAttribute("username", userDto.getUsername());
            redirectAttributes.addFlashAttribute("password", userDto.getPassword());

            // Get the referer URL from the request headers
            String referer = request.getHeader("Referer");
            
            // Redirect to the referer page or default to login if referer is null
            return "redirect:" + (referer != null ? referer : "/home");
        } catch (IOException e) {
            model.addAttribute("error", "Failed to register user.");
            return "register"; // Return to the registration page with error message
        }
    }
    
    @GetMapping("/admin/apitest")
    public String apiTest() {
    	return "admin/apitest";
    }
    
    @GetMapping("/admin/apitest/job/post-api-job")
    public String apiTest_POST_API_JOB() {
    	return "admin/apitest/job/apitest-addjob";
    }
    
    @GetMapping("/admin/apitest/general-test")
    public String apiGeneralTest() {
    	return "admin/apitest/general-test";
    }

}
