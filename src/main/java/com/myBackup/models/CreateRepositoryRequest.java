package com.myBackup.models;

public class CreateRepositoryRequest {
    private String destDirectory;
    private String clientID;

    // Getters and Setters
    public String getDestDirectory() {
        return destDirectory;
    }

    public void setDestDirectory(String destDirectory) {
        this.destDirectory = destDirectory;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
