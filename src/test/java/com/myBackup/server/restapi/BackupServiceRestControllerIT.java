package com.myBackup.server.restapi;

import com.myBackup.MyBackupApplication;
import com.myBackup.client.services.UUIDService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MyBackupApplication.class)
public class BackupServiceRestControllerIT {
    final Logger logger = LoggerFactory.getLogger(BackupServiceRestControllerIT.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UUIDService uuidService;
    
    private String csrfToken;
    //@Autowired
    //private UserService userService; // Add UserService for user registration

    private List<String> cookies; // Store cookies for session management

    @BeforeEach
    public void setUp() throws Exception {
        ResponseEntity<String> loginPageResponse = restTemplate.getForEntity(getBaseUrl() + "", String.class);
        csrfToken = extractCsrfToken(loginPageResponse.getBody());
        HttpHeaders getHeaders = loginPageResponse.getHeaders();
        cookies = getHeaders.get("Set-Cookie"); 
        //logger.info("CSRF Token retrieved from {} is {}", getBaseUrl(), csrfToken);
        
        
    	// Step 1: Perform the initial login
        HttpHeaders postHeaders = new HttpHeaders();
    	postHeaders.add("X-CSRF-TOKEN", csrfToken);
    	postHeaders.put(HttpHeaders.COOKIE, cookies); // Maintain session consistency
    	postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    	// Login form data
    	MultiValueMap<String, String> loginForm = new LinkedMultiValueMap<>();
    	loginForm.add("username", System.getProperty("user.name"));
    	loginForm.add("password", uuidService.getUUID());
    	loginForm.add("_csrf", csrfToken);

    	HttpEntity<MultiValueMap<String, String>> loginEntity = new HttpEntity<>(loginForm, postHeaders);
    	ResponseEntity<String> loginResponse = restTemplate.exchange(
    	    getBaseUrl() + "/login", // Perform the login request
    	    HttpMethod.POST,
    	    loginEntity,
    	    String.class
    	);

    	// Step 2: Check for 302 status code (redirect)
    	String finalCsrfToken = "";
    	List<String> finalCookies = null;
    	
    	if (loginResponse.getStatusCode() == HttpStatus.FOUND) {
    	    // Extract cookies (JSESSIONID etc.) from the response headers
    	    cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
    	    csrfToken = loginResponse.getHeaders().getFirst("X-CSRF-TOKEN");
        	logger.debug("After login, csrfToken is {}, cookies is {}", csrfToken, cookies);

    	    // Follow the redirect to /home
    	    String redirectUrl = loginResponse.getHeaders().getLocation().toString();
    	    HttpHeaders redirectHeaders = new HttpHeaders();
    	    redirectHeaders.put(HttpHeaders.COOKIE, cookies); // Use the login cookies
            redirectHeaders.set("X-CSRF-TOKEN", csrfToken);

    	    HttpEntity<String> redirectEntity = new HttpEntity<>(redirectHeaders);
    	    ResponseEntity<String> finalResponse = restTemplate.exchange(
    	        redirectUrl,
    	        HttpMethod.GET,
    	        redirectEntity,
    	        String.class
    	    );

    	    // Extract the final CSRF token and cookies from the response
    	    finalCsrfToken = extractCsrfTokenFromResponse(finalResponse);
    	    finalCookies = finalResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
    	    if (finalCsrfToken!=null) csrfToken = finalCsrfToken;
    	    if (finalCookies!=null) cookies = finalCookies;

    	    logger.debug("Final response: csrfToken = {}, cookies = {}", finalCsrfToken, finalCookies);
    	}
    	
    }

    private String extractCsrfToken(String html) {
        Document doc = Jsoup.parse(html);
        Element csrfMeta = doc.selectFirst("meta[name=_csrf]"); // Select the CSRF token meta tag
        return csrfMeta != null ? csrfMeta.attr("content") : null; // Get the content attribute
    }
    
    private String extractCsrfTokenFromResponse(ResponseEntity<String> response) {
        // Check if the response body exists
        if (response == null || response.getBody() == null || response.getBody().isEmpty()) {
            logger.warn("Response body is empty. No CSRF token to extract.");
            return null;
        }

        // Check if the response is a redirect (302 Found)
        if (response.getStatusCode().is3xxRedirection()) {
            logger.warn("Response is a redirect ({}). No CSRF token to extract.", response.getStatusCode());
            return null;
        }

        // Get the response body
        String body = response.getBody();
        
        // Simple string search for the CSRF token
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

        return csrfToken; // Return null if the token was not found
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    public void testApiIsWorking() {
    	logger.info("Entering testApiIsWorking...");
    	
    	/*
        ResponseEntity<String> loginPageResponse = restTemplate.getForEntity(getBaseUrl() + "", String.class);
        csrfToken = extractCsrfToken(loginPageResponse.getBody());
        HttpHeaders getHeaders = loginPageResponse.getHeaders();
        cookies = getHeaders.get("Set-Cookie"); 
        //logger.info("CSRF Token retrieved from {} is {}", getBaseUrl(), csrfToken);
        
        
    	// Step 1: Perform the initial login
        HttpHeaders postHeaders = new HttpHeaders();
    	postHeaders.add("X-CSRF-TOKEN", csrfToken);
    	postHeaders.put(HttpHeaders.COOKIE, cookies); // Maintain session consistency
    	postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    	// Login form data
    	MultiValueMap<String, String> loginForm = new LinkedMultiValueMap<>();
    	loginForm.add("username", System.getProperty("user.name"));
    	loginForm.add("password", uuidService.getUUID());
    	loginForm.add("_csrf", csrfToken);

    	HttpEntity<MultiValueMap<String, String>> loginEntity = new HttpEntity<>(loginForm, postHeaders);
    	ResponseEntity<String> loginResponse = restTemplate.exchange(
    	    getBaseUrl() + "/login", // Perform the login request
    	    HttpMethod.POST,
    	    loginEntity,
    	    String.class
    	);

    	// Step 2: Check for 302 status code (redirect)
    	String finalCsrfToken = "";
    	List<String> finalCookies = null;
    	
    	if (loginResponse.getStatusCode() == HttpStatus.FOUND) {
    	    // Extract cookies (JSESSIONID etc.) from the response headers
    	    cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
    	    csrfToken = loginResponse.getHeaders().getFirst("X-CSRF-TOKEN");
        	logger.debug("After login, csrfToken is {}, cookies is {}", csrfToken, cookies);

    	    // Follow the redirect to /home
    	    String redirectUrl = loginResponse.getHeaders().getLocation().toString();
    	    HttpHeaders redirectHeaders = new HttpHeaders();
    	    redirectHeaders.put(HttpHeaders.COOKIE, cookies); // Use the login cookies
            redirectHeaders.set("X-CSRF-TOKEN", csrfToken);

    	    HttpEntity<String> redirectEntity = new HttpEntity<>(redirectHeaders);
    	    ResponseEntity<String> finalResponse = restTemplate.exchange(
    	        redirectUrl,
    	        HttpMethod.GET,
    	        redirectEntity,
    	        String.class
    	    );

    	    // Extract the final CSRF token and cookies from the response
    	    finalCsrfToken = extractCsrfTokenFromResponse(finalResponse);
    	    finalCookies = finalResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
    	    if (finalCsrfToken!=null) csrfToken = finalCsrfToken;
    	    if (finalCookies!=null) cookies = finalCookies;

    	    logger.debug("Final response: csrfToken = {}, cookies = {}", finalCsrfToken, finalCookies);
    	}
    	*/
    	// Step 3: Set the cookies for the subsequent request
    	HttpHeaders headersWithCookies = new HttpHeaders();
    	headersWithCookies.put(HttpHeaders.COOKIE, cookies);
    	headersWithCookies.add("X-CSRF-TOKEN", csrfToken);

    	// Step 5: Make the request to the protected endpoint using the retained session
    	HttpEntity<Void> requestEntity = new HttpEntity<>(headersWithCookies);
    	
    	ResponseEntity<String> response = restTemplate.exchange(
    	    getBaseUrl() + "/api/backup/test",
    	    HttpMethod.GET,
    	    requestEntity,
    	    String.class
    	);

    	logger.info("Response: {}", response);    
	}
    
    
    @Test
    public void testDoesFileHashExist() {
        String repositoryId = "testRepo";
        String hash = "testHash";

        // Prepare headers with the CSRF token
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSRF-TOKEN", csrfToken); // Include CSRF token

        HttpEntity<Void> entity = new HttpEntity<>(headers); // Create the entity with headers

        ResponseEntity<Boolean> response = restTemplate.exchange(
        		getBaseUrl() + "/api/backup/does-file-hash-exist?repositoryId=" + repositoryId + "&hash=" + hash,
                HttpMethod.GET,
                entity,
                Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue(); // Modify based on actual expected result
    }

    //@Test
    public void testUploadBlock() {
        String repositoryId = "testRepo";
        String hash = "testBlockHash";
        byte[] dataBlock = new byte[]{1, 2, 3}; // Sample data block
        boolean encrypt = false;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSRF-TOKEN", csrfToken); // Include CSRF token

        ResponseEntity<String> response = restTemplate.postForEntity(
        		getBaseUrl() + "/api/backup/upload-block?repositoryId=" + repositoryId + "&hash=" + hash + "&encrypt=" + encrypt,
                new HttpEntity<>(dataBlock, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull(); // Check based on your upload logic
    }

    //@Test
    public void testBlockHashExists() {
        String repositoryId = "testRepo";
        String hash = "testHash";

        // Prepare headers with the CSRF token
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSRF-TOKEN", csrfToken); // Include CSRF token

        HttpEntity<Void> entity = new HttpEntity<>(headers); // Create the entity with headers

        ResponseEntity<String> response = restTemplate.exchange(
        		getBaseUrl() + "/api/backup/does-block-hash-exist?repositoryId=" + repositoryId + "&hash=" + hash,
                HttpMethod.GET,
                entity,
                String.class); // Get the response as a String

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody()); // Log the raw response body

        // Assert the response
        //assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Optionally convert the response body to BooleanResponse if needed.
    }
    
    /*
    @Test
    public void testCommitBackup() {
        // Backup backupRequest = new Backup(null, null, null, null, null);
        // backupRequest.setFileName("example.txt"); // Set properties as needed

        // Prepare headers with the CSRF token
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSRF-TOKEN", csrfToken); // Include CSRF token

        HttpEntity<Backup> entity = new HttpEntity<>(backupRequest, headers); // Create entity with headers

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/backup/commit-backup",
                entity,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull(); // Check based on your commit logic
    }
    */
}
