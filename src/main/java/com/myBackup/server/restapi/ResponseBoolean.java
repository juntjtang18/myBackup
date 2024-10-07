package com.myBackup.server.restapi;

public class ResponseBoolean {
    private boolean exists;

    // Constructor
    public ResponseBoolean(boolean exists) {
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
