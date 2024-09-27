package com.myBackup.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientIDRequest {
    private String clientID;

    // Default constructor
    public ClientIDRequest() {
    }

    // Constructor with @JsonCreator to support JSON deserialization
    @JsonCreator
    public ClientIDRequest(@JsonProperty("clientID") String clientID) {
        this.clientID = clientID;
    }

    // Getter and setter
    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
