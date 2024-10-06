package com.myBackup.services.bfs;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockSchema {
    private int size;                 // size of the data block
    private int referenceCount;       // Count of references to this block
    private boolean encrypted;      // Flag to indicate if the block is encrypted
    private long createdTimestamp;    // Timestamp for when the block was created
    private long modifiedTimestamp;   // Timestamp for when the block was last modified
    private final int version;        // Version number of the block schema (currently version 1)
    private String hash;              // Hash of the block data

    // Constructor for version 1
    public BlockSchema(int size, int referenceCount, boolean isEncrypted, long createdTimestamp, long modifiedTimestamp, String hash) {
        this.size = size;
        this.referenceCount = referenceCount;
        this.encrypted = isEncrypted;
        this.createdTimestamp = createdTimestamp;
        this.modifiedTimestamp = modifiedTimestamp;
        this.version = 1; // Fixed at version 1 for now
        this.hash = hash; // Store the hash
    }

    // Getters
    public int getReferenceCount() {
        return referenceCount;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public int getVersion() {
        return version;
    }

    public int getSize() {
        return size;
    }

    public String getHash() {
        return hash;
    }

    // Setters
    public void setReferenceCount(int referenceCount) {
        if (referenceCount >= 0) {
            this.referenceCount = referenceCount;
        } else {
            throw new IllegalArgumentException("Reference count cannot be negative.");
        }
    }

    public void setEncrypted(boolean encrypted) {
        encrypted = encrypted;
    }

    public void setSize(int size) {
        if (size >= 0) {
            this.size = size;
        } else {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
    }

    // Method to get the size of serialized schema in bytes
    public int getSerializedSize() {
        return Integer.BYTES +   // size for version (int)
               Integer.BYTES +   // size for size
               Integer.BYTES +   // size for referenceCount (int)
               Byte.BYTES +      // size for isEncrypted (boolean, stored as byte)
               Long.BYTES +      // size for createdTimestamp (long)
               Long.BYTES +      // size for modifiedTimestamp (long)
               Integer.BYTES +   // size for hash length (int)
               hash.getBytes().length; // size for hash string
    }

    // Method to write the schema to a DataOutputStream
 // Method to write the schema to a RandomAccessFile
    public void writeTo(RandomAccessFile raf) throws IOException {
        raf.writeInt(version);
        raf.writeInt(size);
        raf.writeInt(referenceCount);
        raf.writeBoolean(encrypted);
        raf.writeLong(createdTimestamp);
        raf.writeLong(modifiedTimestamp);
        
        // Write the hash
        byte[] hashBytes = hash.getBytes();
        raf.writeInt(hashBytes.length); // Write the length of the hash
        raf.write(hashBytes); // Write the hash itself
    }

 // Method to read the schema from a RandomAccessFile
    public static BlockSchema readFrom(RandomAccessFile raf) throws IOException {
        // Read the schema directly from the RandomAccessFile
    	int schemaSize = raf.readInt();
        int version = raf.readInt(); // Read the version from the file
        if (version == 1) {
            int size = raf.readInt(); // Read the size
            int referenceCount = raf.readInt(); // Read the reference count
            boolean isEncrypted = raf.readBoolean(); // Read the encryption flag
            long createdTimestamp = raf.readLong(); // Read the created timestamp
            long modifiedTimestamp = raf.readLong(); // Read the modified timestamp

            // Read the hash
            int hashLength = raf.readInt(); // Read the length of the hash
            byte[] hashBytes = new byte[hashLength]; // Create an array for the hash
            raf.readFully(hashBytes); // Read the hash into the array
            String hash = new String(hashBytes); // Convert the hash to a String

            return new BlockSchema(size, referenceCount, isEncrypted, createdTimestamp, modifiedTimestamp, hash);
        } else {
        	throw new IllegalArgumentException("Unsupported schema version");
        }
    }

    // Method to increment the reference count
    public void incrementReferenceCount() {
        this.referenceCount++;
    }

    // Method to decrement the reference count
    public void decrementReferenceCount() {
        if (this.referenceCount > 0) {
            this.referenceCount--;
        }
    }

    // Method to compute the hash of the block data
    public static String computeHash(byte[] blockData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(blockData);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
