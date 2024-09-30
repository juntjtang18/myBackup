package com.myBackup.services.BlockStorage;

public interface Encryptor {
    byte[] encrypt(byte[] data);
    byte[] decrypt(byte[] data);
}
