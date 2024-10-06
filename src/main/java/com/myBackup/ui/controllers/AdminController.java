package com.myBackup.ui.controllers;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myBackup.models.UserDto;
import com.myBackup.security.User;
import com.myBackup.security.UserRepository;
import com.myBackup.security.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminController {
	@Autowired
    private UserService userService;
	@Autowired
    private UserRepository userRepository;
	
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    //@RequiresRoles("admin") // Require admin role for user registration
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDto") UserDto userDto, 
                               Model model, 
                               RedirectAttributes redirectAttributes, 
                               HttpServletRequest request) {
        try {
            // Check if the username already exists
            Optional<User> existingUser = userRepository.findByUsername(userDto.getUsername());
            if (!existingUser.isEmpty()) {
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
            
            // Redirect to the referer page or default to home if referer is null
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
