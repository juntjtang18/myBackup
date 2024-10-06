package com.myBackup.server.restapi;

public class BooleanResponse {
    private boolean exists;

    // Constructor
    public BooleanResponse(boolean exists) {
        this.exists = exists;
    }

    // Getter
    public boolean isExists() {
        return exists;
    }

    // Setter (optional)
    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
