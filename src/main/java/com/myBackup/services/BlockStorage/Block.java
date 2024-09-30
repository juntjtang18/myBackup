package com.myBackup.services.BlockStorage;

import com.myBackup.services.BlockStorage.BlockSchema;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;

public class Block {
    private final BlockSchema schema;
    private byte[] data;

    public Block(BlockSchema schema, byte[] data) {
        this.schema = schema;
        this.data = data;
    }
    
    public Block(byte[] data, boolean encrypt, String hash) throws NoSuchAlgorithmException {
        this.data = data;
        this.schema = new BlockSchema(this.data.length, 1, encrypt, System.currentTimeMillis(), System.currentTimeMillis(), hash);
    }

    public BlockSchema getSchema() {
        return schema;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isEncrypted() {
        return schema.isEncrypted();
    }

    public int getSize() {
        return schema.getSize();
    }

    public void encrypt(Encryptor encryptor) throws IOException {
        if (schema.isEncrypted()) {
            data = encryptor.encrypt(data);
            schema.setSize(data.length); // Update size after encryption
        }
    }

    public void decrypt(Encryptor encryptor) throws IOException {
        if (schema.isEncrypted()) {
            data = encryptor.decrypt(data);
        }
    }

    public void writeTo(RandomAccessFile file) throws IOException {
        int schemaSize = schema.getSerializedSize();
        file.write(intToByteArray(schemaSize)); // Write the schema size
        schema.writeTo(file); // Write the BlockSchema
        file.write(data); // Write the block data
    }
    
    public static Block readFromFile(RandomAccessFile file, Encryptor encryptor) throws IOException, NoSuchAlgorithmException {
        BlockSchema blockSchema = BlockSchema.readFrom(file);
        byte[] blockData = new byte[blockSchema.getSize()];
        file.readFully(blockData);
        if (blockSchema.isEncrypted()) {
            blockData = encryptor.decrypt(blockData); // Decrypt if needed
        }
        return new Block(blockSchema, blockData);
    }

    private byte[] intToByteArray(int value) {
        return new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
    }
}
