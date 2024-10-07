package com.myBackup.server.restapi;

public class RequestClientID {
    private String clientID;

    // Constructor with @JsonCreator to support JSON deserialization
    //@JsonCreator
    //public ClientIDRequest(@JsonProperty("clientID") String clientID) {
    //    this.clientID = clientID;
    //}

    public RequestClientID(String clientID2) {
		this.clientID = clientID2;
	}

	// Getter and setter
    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
