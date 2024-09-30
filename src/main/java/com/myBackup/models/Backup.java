package com.myBackup.models;

import java.io.Serializable;

public class Backup implements Serializable {
	private static final long serialVersionUID = 1L;
	private String creator;
    private String srcDir;
    private String dstDir;

    public Backup(String creator, String sourceDirectory, String backupDirectory) {
        this.setCreator(creator);
        this.setSrcDir(sourceDirectory);
        this.setDstDir(backupDirectory);
    }

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
	}

	public String getDstDir() {
		return dstDir;
	}

	public void setDstDir(String dstDir) {
		this.dstDir = dstDir;
	}

}
