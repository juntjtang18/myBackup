package com.myBackup.services.bfs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBackup.MyBackupApplication;
import com.myBackup.config.Config;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MyBackupApplication.class)
public class RepositoryStorageTest {
	@Autowired
    private ObjectMapper objectMapper; // Injected ObjectMapper
	@Autowired
    private Config config;
	@Autowired
	private RepositoryStorage repoStorage;
	
	@Test
	public void testCreatRepository() {
		System.out.println("I am here.");
		String repoID = "testRepoID";
		String clientID = UUID.randomUUID().toString();
		
		try {
			repoStorage.delete(repoID);
			
			Repository repo = new Repository();
			repo.setRepoID("testRepoID");
			repo.setDestDirectory("d:\\backupRepo");
			repo.setServerName("localhost");
			repo.setServerUrl("http://localhost:8080");
			repo.getClientIDs().add(clientID);
			repoStorage.createRepository(repo);
			
			Repository repo1 = repoStorage.getRepositoryById("testRepoID");
			assertThat(repo1!=null);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
