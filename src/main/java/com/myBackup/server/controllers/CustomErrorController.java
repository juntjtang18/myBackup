package com.myBackup.server.controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController {

    private final ResourceLoader resourceLoader;

    public CustomErrorController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestURI = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);

        // Check if the request is for a static resource
        if (requestURI != null && requestURI.startsWith("/images/")) {
            // Load the default placeholder image
            Resource resource = resourceLoader.getResource("classpath:static/images/default-placeholder.png");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/png")
                    .body(resource);
        }

        // Handle other errors as you see fit (optional)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}

