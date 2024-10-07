package com.myBackup.server.restapi;

import com.myBackup.client.services.UUIDService;
import com.myBackup.services.bfs.Repository;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BackupServiceRestControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(BackupServiceRestControllerTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;
    @Autowired
    private UUIDService uuidService;
    private ITLogin itLogin;
    private static final String TEST_DIRECTORY = "d:\\backupRepo";


    @BeforeEach
    public void setUp() throws IOException {
        itLogin = new ITLogin(restTemplate, port, uuidService);
        itLogin.login();  // Assuming this method logs in and sets headers
    }

    @Test
    public void testUploadBlockToRepository() throws IOException {
        // Step 1: Check if the test directory exists
        File testDirectory = new File(TEST_DIRECTORY);
        
        if (testDirectory.exists()) {
            // If the directory exists, delete it and all its contents
            deleteDirectoryWithFiles(testDirectory);
            System.out.println("Existing test directory and its contents have been deleted.");
        }
        
        // Step 2: Create a new repository (as the directory is now deleted)
        logger.info("Start to create repository for upload block testing...");
        String createRepoUrl = getBaseUrl() + "/api/repositories/create";
        
        RequestCreateRepository createRepositoryRequest = new RequestCreateRepository();
        createRepositoryRequest.setDestDirectory(TEST_DIRECTORY);
        createRepositoryRequest.setClientID("testClientID");
        createRepositoryRequest.setServerUrl("http://localhost:8080");
        createRepositoryRequest.setServerName("localhost");

        HttpHeaders headers = itLogin.getCommonHeaders();
        HttpEntity<RequestCreateRepository> createEntity = new HttpEntity<>(createRepositoryRequest, headers);

        ResponseEntity<Repository> createResponse = restTemplate.postForEntity(createRepoUrl, createEntity, Repository.class);

        // Assert the response to ensure the repository is created
        assertNotNull(createResponse);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        logger.info("Repository created successfully {} ", createResponse.getBody().getRepoID());

        // Step 3: Upload a block to the repository
        String uploadBlockUrl = getBaseUrl() + "/api/backup/upload-block";

        byte[] dataBlock = "Test Data Block".getBytes(); // Example data block
        String hash = "exampleHash123"; // Example hash value
        boolean encrypt = false; // Assume no encryption for this test

        HttpEntity<byte[]> uploadEntity = new HttpEntity<>(dataBlock, headers);

        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
            uploadBlockUrl + "?repositoryId=" + createResponse.getBody().getRepoID() + "&hash=" + hash + "&encrypt=" + encrypt,
            uploadEntity,
            String.class);

        // Assert the upload response
        assertNotNull(uploadResponse);
        assertEquals(HttpStatus.OK, uploadResponse.getStatusCode());
        assertEquals(hash, uploadResponse.getBody());  // Verify the hash in the response
    }

    private void deleteDirectoryWithFiles(File directory) throws IOException {
        Files.walk(directory.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }
}
