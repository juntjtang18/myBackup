package com.myBackup.server.restapi;

import org.springframework.web.bind.annotation.*;

import com.myBackup.services.bfs.RepositoryService;
import com.myBackup.services.bfs.RepositoryServiceFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/backup")
public class BackupServiceRestController {
	@Autowired
    private RepositoryServiceFactory repositoryServiceFactory;
    //private RepositoryService repoService;
    
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("API is working!");
    }
    
    @GetMapping("/does-file-hash-exist")
    @ResponseBody
    public ResponseEntity<Boolean> doesFileHashExist(
            @RequestParam("repositoryId") String repositoryId,
            @RequestParam("hash") String hash) {
        
        RepositoryService repositoryService = repositoryServiceFactory.getRepositoryService(repositoryId);
        
        try {
            boolean exists = hash != null && repositoryService.fileHashExists(hash);
            return ResponseEntity.ok()
                                 .contentType(MediaType.APPLICATION_JSON) // Set content type to JSON
                                 .body(exists); // Return 200 OK with the result
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .contentType(MediaType.APPLICATION_JSON) // Set content type to JSON
                                 .body(null); // Return 500 Internal Server Error
        }
    }

    
    @PostMapping("/upload-block")
    @ResponseBody
    public ResponseEntity<String> uploadBlock(
            @RequestParam("repositoryId") String repositoryId,
            @RequestParam("hash") String hash,  		
            @RequestParam("encrypt") boolean encrypt,  
            @RequestBody byte[] dataBlock) {
        
        // Get the RepositoryService for the given repositoryId
        RepositoryService repositoryService = repositoryServiceFactory.getRepositoryService(repositoryId);
        
        try {
            String resultHash = repositoryService.uploadBlock(hash, dataBlock, encrypt);
            return ResponseEntity.ok(resultHash);  // Return 200 OK with the resulting hash
        } catch (NoSuchAlgorithmException | IOException e) {
            // Log the error and return a bad request response
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error processing upload: " + e.getMessage());
        }
    }

    @GetMapping("/does-block-hash-exist")
    @ResponseBody
    public ResponseEntity<ResponseBoolean> blockHashExists(@RequestParam("repositoryId") String repositoryId, @RequestParam("hash") String hash) {
        
        // Print the input parameters for debugging
        System.out.println("Received repositoryId: " + repositoryId);
        System.out.println("Received hash: " + hash);

        RepositoryService repositoryService = repositoryServiceFactory.getRepositoryService(repositoryId);
        
        try {
            boolean exists = repositoryService.blockHashExists(hash);
            ResponseBoolean response = new ResponseBoolean(exists);
            return ResponseEntity.ok()
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body(response); // Return JSON response
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body(new ResponseBoolean(false)); // Return a JSON error response
        }
    }

    
}


