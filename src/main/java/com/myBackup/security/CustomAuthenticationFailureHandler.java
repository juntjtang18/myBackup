package com.myBackup.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
	                                      HttpServletResponse response,
	                                      AuthenticationException exception) throws IOException {
	    // Log failure for debugging
	    System.out.println("Authentication failed: " + exception.getMessage());
	    response.sendRedirect("/login?error=true");
	}
}
