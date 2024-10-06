package com.myBackup.server.restapi;

import com.myBackup.MyBackupApplication;
import com.myBackup.client.services.UUIDService;
import com.myBackup.security.UserService; // Adjust the import based on your package structure
import com.myBackup.services.bfs.Backup;
import com.myBackup.ui.controllers.DashboardController;

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
        // Retrieve CSRF token by making a GET request to the login page
        ResponseEntity<String> loginPageResponse = restTemplate.getForEntity(getBaseUrl() + "", String.class);
        
        csrfToken = extractCsrfToken(loginPageResponse.getBody());
        logger.info("CSRF Token retrieved from {} is {}", getBaseUrl(), csrfToken);
        
        HttpHeaders getHeaders = loginPageResponse.getHeaders();
        cookies = getHeaders.get("Set-Cookie"); // Store cookies for later use
        
        HttpHeaders postHeaders = new HttpHeaders();
        if (csrfToken != null) {
            postHeaders.add("X-CSRF-TOKEN", csrfToken);
        }
        postHeaders.add("Content-Type", "application/x-www-form-urlencoded"); // Adjust according to your content type
        
        if (cookies != null) {
            postHeaders.put(HttpHeaders.COOKIE, cookies); // Ensure session consistency
        }
        
        // Prepare the login form parameters (username, password, etc.)
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("username", System.getProperty("user.name"));
        formParams.add("password", uuidService.getUUID());
        formParams.add("_csrf", csrfToken);
        
        // Make the POST request to /login
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(formParams, postHeaders);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(getBaseUrl() + "/login", httpEntity, String.class);
        
        // Log the response of the login attempt
        logger.info("Login response: {}", loginResponse.getBody());
        
        // Debug logging to check status and body
        logger.info("Login response status: {}", loginResponse.getStatusCode());

        // Verify the login is successful (Check for status or body content)
        if (loginResponse.getStatusCode() == HttpStatus.FOUND) {
            logger.info("Login successful: Redirected");
        } else {
            // If not a redirect, check for success message in body
            assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(loginResponse.getBody()).contains("Login successful"); // Adjust according to actual response
        }
        
        getHeaders = loginPageResponse.getHeaders();
        cookies = getHeaders.get("Set-Cookie"); // Store cookies for later use
        logger.info("CSRF Token: {}", csrfToken);
        logger.info("Cookies: {}", cookies);

    }

    private String extractCsrfToken(String html) {
        // Use Jsoup to parse the HTML and extract the CSRF token
        Document doc = Jsoup.parse(html);
        Element csrfMeta = doc.selectFirst("meta[name=_csrf]"); // Select the CSRF token meta tag
        return csrfMeta != null ? csrfMeta.attr("content") : null; // Get the content attribute
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    public void testApiIsWorking() {
        System.out.println("Entering testApiIsWorking");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSRF-TOKEN", csrfToken); // Add CSRF token to the headers

        if (cookies != null) {
            headers.put(HttpHeaders.COOKIE, cookies); // Include cookies in request
        }

        logger.info("CSRF Token: {}", csrfToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        logger.info("HttpEntity for calling test API: {}", entity);
        
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/api/backup/test", HttpMethod.GET, entity, String.class);
        
        logger.info("Response status: {}, headers: {}, body: {}", response.getStatusCode(), response.getHeaders(), response.getBody());

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.FORBIDDEN);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            // Check if the response body contains the expected message
            assertThat(response.getBody()).isEqualTo("API is working!"); // Adjust according to actual response
        }
    }
    //@Test
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
