package com.myBackup.server.restapi;

import com.myBackup.MyBackupApplication;
import com.myBackup.client.services.UUIDService;
import com.myBackup.services.bfs.Repository;
import org.junit.jupiter.api.AfterEach;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MyBackupApplication.class)
class RepositoryServiceRestControllerIT {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceRestControllerIT.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private UUIDService uuidService;

    private ITLogin itLogin;

    private static final String TEST_DIRECTORY = "d:\\backupRepo";

    @BeforeEach
    void setUp() {
        itLogin = new ITLogin(restTemplate, port, uuidService);
        itLogin.login();

        File dir = new File(TEST_DIRECTORY);
        if (dir.exists()) {
            logger.info("{} exists, deleting the directory and its contents", TEST_DIRECTORY);
            deleteDirectoryRecursively(dir);
        }
    }

    private void deleteDirectoryRecursively(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectoryRecursively(file);
            }
        }
        directory.delete();
    }

    @AfterEach
    void tearDown() {
        File dir = new File(TEST_DIRECTORY);
        if (dir.exists()) {
            dir.delete();
        }
    }

    //@Test
    void testGetRepositories() {
        String clientID = "testClientID";
        String expectedResponse = "Received clientID: " + clientID;

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/repositories/list?clientID=" + clientID,
                String.class,
                itLogin.getCommonHeaders()
        );

        assertThat(response.getStatusCode().equals(HttpStatus.OK));
        assertThat(expectedResponse.equals(response.getBody()));
    }

    //@Test
    void testGetAllRepositories() {
        String clientID = "testClientID";
        RequestClientID request = new RequestClientID(clientID);
        List<Repository> mockRepositories = new ArrayList<>();
        mockRepositories.add(new Repository("Repo1", clientID));
        mockRepositories.add(new Repository("Repo2", clientID));

        //when(backupReposService.getAllByClientID(clientID)).thenReturn(mockRepositories);

        ResponseEntity<?> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/repositories/list-by-clientID",
                request,
                List.class,
                itLogin.getCommonHeaders()
        );

        // List<Repository> repositories = (List<Repository>) response.getBody();

        assertThat(response.getStatusCode().equals(HttpStatus.OK));
        assertEquals(mockRepositories, (List<Repository>) response.getBody());
    }

    @Test
    void testCreateRepositoryWithNonExistingDirectory() {
        RequestCreateRepository request = new RequestCreateRepository();
        request.setDestDirectory(TEST_DIRECTORY);
        request.setClientID("testClientID");
        request.setServerUrl("http://localhost:8080");
        request.setServerName("localhost");

        HttpHeaders headers = itLogin.getCommonHeaders();
        HttpEntity<RequestCreateRepository> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<Repository> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/repositories/create",
                httpEntity,
                Repository.class
        );

        logger.info("response for create: {}", response);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(request.getDestDirectory(), response.getBody().getDestDirectory());
        //assertEquals(request.getClientID(), response.getBody().getClientID());
    }

    @Test
    void testCreateRepositoryWithExistingDirectory() {
        new File(TEST_DIRECTORY).mkdirs();

        RequestCreateRepository request = new RequestCreateRepository();
        request.setDestDirectory(TEST_DIRECTORY);
        request.setClientID("testClientID");
        request.setServerUrl("http://localhost:8080");
        request.setServerName("localhost");

        HttpHeaders headers = itLogin.getCommonHeaders();
        HttpEntity<RequestCreateRepository> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/repositories/create",
                httpEntity,
                String.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testConfirmCreateRepository() {
        RequestCreateRepository request = new RequestCreateRepository();
        request.setDestDirectory(TEST_DIRECTORY);
        request.setClientID("testClientID");
        request.setServerUrl("http://localhost:8080");
        request.setServerName("localhost");

        HttpHeaders headers = itLogin.getCommonHeaders();
        HttpEntity<RequestCreateRepository> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<Repository> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/repositories/create/confirm",
                httpEntity,
                Repository.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // You can assert more specific properties if needed
        assertEquals(request.getDestDirectory(), response.getBody().getDestDirectory());
        //assertEquals(request.getClientID(), response.getBody().getClientID());
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }
}
