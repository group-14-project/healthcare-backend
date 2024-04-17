package com.example.server.aws;

import com.amazonaws.util.IOUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
@Service
public class EncryptFile {
    private String key = "aditya0000000000";
    public byte[] encryptFile(InputStream inputStream) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

        // Create AES cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Encrypt input stream
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                IOUtils.copy(inputStream, cipherOutputStream);
            }
            return outputStream.toByteArray();
        }
    }

    public byte[] decryptFile(byte[] encryptedBytes) throws Exception {
        // Convert key to AES key spec
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

        // Create AES cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Decrypt input stream
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);
             CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copy(cipherInputStream, outputStream);
            return outputStream.toByteArray();
        }
    }

}
