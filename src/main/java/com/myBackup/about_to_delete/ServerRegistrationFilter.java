package com.myBackup.about_to_delete;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.myBackup.client.services.ServerRegistrationService;

//@Order(2)
//@Component
public class ServerRegistrationFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerRegistrationFilter.class);

    //@Autowired
    //private ServerRegistration serverRegistration;
    private final ServerRegistrationService serverRegistrationService; // Dependency
    //private final UUIDService uuidService;
    
    // Constructor injection for ServerRegistration
    public ServerRegistrationFilter(ServerRegistrationService serverRegistrationService) {
        this.serverRegistrationService = serverRegistrationService;
        //this.uuidService = uuidService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing ServerRegistrationFilter");
        // Additional initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //logger.info("Filter triggered for request: {}", ((HttpServletRequest) request).getRequestURI());

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientAddress = request.getRemoteAddr(); // Get the client address
        String serverName = httpRequest.getServerName(); // Get the server name
        int serverPort = httpRequest.getServerPort(); // Get the server port
        String scheme = httpRequest.getScheme(); // Get the scheme (http or https)

        // Construct the full server address
        String targetServer = scheme + "://" + serverName + ":" + serverPort; 

        //logger.debug("Request client address: {}, server address: {}", clientAddress, targetServer);

        // Check if the request is for the /register-to-server endpoint
        if (httpRequest.getRequestURI().equals("/register-to-server")) {
            chain.doFilter(request, response); // Continue with the next filter or target resource
            return;
        }
        
        // Check if the target server is registered
        boolean isRegistered = serverRegistrationService.isRegistered(targetServer);

        if (!isRegistered) {
            // If the client address matches the server's localhost address
            boolean isSameMachine = "127.0.0.1".equals(clientAddress) || 
                                    "0:0:0:0:0:0:0:1".equals(clientAddress) || 
                                    serverName.equals(clientAddress);

            if (isSameMachine) {
                // Perform automatic registration
                serverRegistrationService.registerToLocalServer(targetServer);
                logger.info("Automatic registration completed to server: {}", targetServer);
                // Proceed to the next filter or resource
                chain.doFilter(request, response);
                return;
            } else {
                // Route to register-to-server if not localhost
                httpResponse.sendRedirect("/register-to-server");
                return;
            }
        }

        // If registered, continue to the next filter or resource
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        logger.info("Destroying ServerRegistrationFilter");
        // Cleanup code if necessary
    }
}
