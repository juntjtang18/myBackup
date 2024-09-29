package com.myBackup.server.restapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UtilityRestController {

    @GetMapping("/api/uuid")
    public String getUUID() {
        return UUID.randomUUID().toString(); // Return the UUID as a String
    }
}
