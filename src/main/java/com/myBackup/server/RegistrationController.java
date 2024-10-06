package com.myBackup.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

import com.myBackup.security.TokenInfo;

@RestController
public class RegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private RegisteredClientsService registeredClientsService;

    @PostMapping("/register-to-server")
    public TokenInfo registerClient(
        @RequestParam String uuid,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String email,
        HttpServletRequest request // Inject HttpServletRequest via method parameter
    ) {
        logger.debug("RegisterClient POST /register-to-server called with uuid: {}, username: {}, email: {}", uuid, username, email);

        // Check if the request comes from the same machine
        if (isRequestFromLocalHost(request)) {
            logger.debug("Request is from localhost.");
            return registeredClientsService.registerLocalClient(uuid, username);
        } else {
            return registeredClientsService.registerRemoteClient(uuid, username, email);
        }
    }

    private boolean isRequestFromLocalHost(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || 
               "localhost".equals(remoteAddr) || isRequestFromLocalNetwork(remoteAddr);
    }

    private boolean isRequestFromLocalNetwork(String remoteAddr) {
        try {
            InetAddress address = InetAddress.getByName(remoteAddr);
            return address.isLoopbackAddress() || address.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}

