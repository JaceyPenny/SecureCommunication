package com.jacemcpherson;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;

public class AESEncryptionUtil {

    private static SecretKey sSecretKey;
    private static Cipher sEncryptCipher;
    private static Cipher sDecryptCipher;

    public static boolean isInitialized() {
        return sSecretKey != null;
    }

    public static SecretKey getSecretKey() {
        if (!isInitialized()) {
            setupAES();
        }

        return sSecretKey;
    }

    public static byte[] getSecretKeyEncoded() {
        return getSecretKey().getEncoded();
    }

    public static void decodeSecretKey(byte[] secretKey) {
        sSecretKey = new SecretKeySpec(secretKey, 0, secretKey.length, "AES");

        setupCiphers();
    }

    public static byte[] encryptMessage(byte[] inMessage) {
        if (!isInitialized()) {
            setupAES();
        }

        try {
            return sEncryptCipher.doFinal(inMessage);
        } catch (Exception e) {
            Console.exception(e);
            return null;
        }
    }

    public static byte[] decryptMessage(byte[] inMessage) {
        if (!isInitialized()) {
            setupAES();
        }

        try {
            return sDecryptCipher.doFinal(inMessage);
        } catch (Exception e) {
            Console.exception(e);
            return null;
        }
    }

    public static byte[] generateRandomMessage(int length) {
        byte[] message = new byte[length];

        Random randomGenerator = new Random();
        randomGenerator.nextBytes(message);

        return message;
    }

    private static void setupAES() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);

            sSecretKey = keyGenerator.generateKey();

            setupCiphers();
        } catch (Exception e) {
            Console.exception(e);
            sSecretKey = null;
        }
    }

    private static void setupCiphers() {
        try {
            sEncryptCipher = Cipher.getInstance("AES");
            sEncryptCipher.init(Cipher.ENCRYPT_MODE, sSecretKey);

            sDecryptCipher = Cipher.getInstance("AES");
            sDecryptCipher.init(Cipher.DECRYPT_MODE, sSecretKey);
        } catch (Exception e) {
            Console.exception(e);
            sEncryptCipher = null;
            sDecryptCipher = null;
        }
    }
}
