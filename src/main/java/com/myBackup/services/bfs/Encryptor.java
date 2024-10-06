package com.myBackup.services.bfs;

public interface Encryptor {
    byte[] encrypt(byte[] data);
    byte[] decrypt(byte[] data);
}
