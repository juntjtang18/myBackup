package com.myBackup.server.restapi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.myBackup.client.services.UUIDService;

import java.util.List;

public class ITLogin {
    private static final Logger logger = LoggerFactory.getLogger(ITLogin.class);
    
    private TestRestTemplate restTemplate;
    private UUIDService uuidService;
    private int port;
    private String csrfToken;
    private List<String> cookies;

    public ITLogin(TestRestTemplate restTemplate, int port, UUIDService uuidService) {
    	this.restTemplate = restTemplate;
    	this.port = port;
    	this.uuidService = uuidService;
    }
    public LoginResponse login() {
        // Perform login and retrieve CSRF token and cookies
        performLogin();
        return new LoginResponse(csrfToken, cookies);
    }

    private void performLogin() {
        ResponseEntity<String> loginPageResponse = restTemplate.getForEntity(getBaseUrl() + "/login", String.class);
        csrfToken = extractCsrfToken(loginPageResponse.getBody());
        cookies = loginPageResponse.getHeaders().get("Set-Cookie");

        logger.info("CSRF Token retrieved from {} is {}", getBaseUrl(), csrfToken);

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.add("X-CSRF-TOKEN", csrfToken);
        postHeaders.put(HttpHeaders.COOKIE, cookies);
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> loginForm = new LinkedMultiValueMap<>();
        loginForm.add("username", System.getProperty("user.name"));
        loginForm.add("password", uuidService.getUUID());
        loginForm.add("_csrf", csrfToken);

        HttpEntity<MultiValueMap<String, String>> loginEntity = new HttpEntity<>(loginForm, postHeaders);
        ResponseEntity<String> loginResponse = restTemplate.exchange(
            getBaseUrl() + "/login",
            HttpMethod.POST,
            loginEntity,
            String.class
        );

        if (loginResponse.getStatusCode() == HttpStatus.FOUND) {
            List<String> loginCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
            String loginCsrfToken = loginResponse.getHeaders().getFirst("X-CSRF-TOKEN");
            cookies = loginCookies == null? cookies:loginCookies;
            csrfToken = loginCsrfToken == null? csrfToken:loginCsrfToken;
            
            String redirectUrl = loginResponse.getHeaders().getLocation().toString();
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.put(HttpHeaders.COOKIE, cookies);
            redirectHeaders.set("X-CSRF-TOKEN", csrfToken);

            HttpEntity<String> redirectEntity = new HttpEntity<>(redirectHeaders);
            ResponseEntity<String> finalResponse = restTemplate.exchange(
                redirectUrl,
                HttpMethod.GET,
                redirectEntity,
                String.class
            );

            String homeCsrfToken = extractCsrfTokenFromResponse(finalResponse);
            List<String> homeCookies = finalResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
            csrfToken = homeCsrfToken == null? csrfToken:homeCsrfToken;
            cookies = homeCookies == null? cookies:homeCookies;
            
            logger.debug("Final response: csrfToken = {}, cookies = {}", csrfToken, cookies);
        }
    }

    public HttpHeaders getCommonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", csrfToken);
        if (cookies != null) {
            headers.put(HttpHeaders.COOKIE, cookies);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String extractCsrfToken(String html) {
        Document doc = Jsoup.parse(html);
        Element csrfMeta = doc.selectFirst("meta[name=_csrf]");
        return csrfMeta != null ? csrfMeta.attr("content") : null;
    }
    
    private String extractCsrfTokenFromResponse(ResponseEntity<String> response) {
        if (response == null || response.getBody() == null || response.getBody().isEmpty()) {
            logger.warn("Response body is empty. No CSRF token to extract.");
            return null;
        }

        if (response.getStatusCode().is3xxRedirection()) {
            logger.warn("Response is a redirect ({}). No CSRF token to extract.", response.getStatusCode());
            return null;
        }

        String body = response.getBody();
        
        String csrfToken = null;
        String tokenPattern = "name=\"_csrf\" content=\"";
        int startIndex = body.indexOf(tokenPattern);
        if (startIndex != -1) {
            startIndex += tokenPattern.length();
            int endIndex = body.indexOf("\"", startIndex);
            if (endIndex != -1) {
                csrfToken = body.substring(startIndex, endIndex);
                logger.info("CSRF token extracted successfully: {}", csrfToken);
            } else {
                logger.warn("End of CSRF token not found.");
            }
        } else {
            logger.warn("CSRF token not found in the response body.");
        }

        return csrfToken;
    }

    private String getBaseUrl() {
        return "http://localhost:" + port; // Use the random port
    }

    public static class LoginResponse {
        private final String csrfToken;
        private final List<String> cookies;

        public LoginResponse(String csrfToken, List<String> cookies) {
            this.csrfToken = csrfToken;
            this.cookies = cookies;
        }

        public String getCsrfToken() {
            return csrfToken;
        }

        public List<String> getCookies() {
            return cookies;
        }
    }
}
