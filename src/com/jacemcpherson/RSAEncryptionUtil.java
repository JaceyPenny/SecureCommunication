package com.jacemcpherson;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * This class provides quick access to functionality such as:<br>
 * * Generating an RSA public/private key pair.
 * * Encoding/decoding RSA public keys
 * * Encrypting messages using partner RSA public key
 * * Decrypting messages using RSA private key
 * * Signing messages using RSA private key
 * * Verifying signatures using partner RSA public key.
 *
 * <br><br>
 * Key size is default to 2048 bits.
 */
public class RSAEncryptionUtil {

    private static int KEY_SIZE = 2048;

    private static KeyPair sKeyPair;

    private static PublicKey sPartnerPublicKey;

    public static PrivateKey getPrivateKey() {
        if (sKeyPair == null) {
            generateRsaKeyPair();
        }
        return sKeyPair.getPrivate();
    }

    public static PublicKey getPublicKey() {
        if (sKeyPair == null) {
            generateRsaKeyPair();
        }
        return sKeyPair.getPublic();
    }

    public static PublicKey getPartnerPublicKey() {
        return sPartnerPublicKey;
    }

    public static byte[] getPublicKeyEncoded() {
        return getPublicKey().getEncoded();
    }

    public static void decodePublicKey(byte[] publicKey) {
        try {
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
            sPartnerPublicKey = rsaKeyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Console.d("This machine does not support RSA encryption methods.");
        }
    }

    public static byte[] decryptMessage(byte[] partnerBytes) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
            return cipher.doFinal(partnerBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Console.d("This machine does not support RSA encryption methods.");
        } catch (InvalidKeyException e) {
            // Shouldn't ever hit this block
            Console.d("There was an error with the private key: It may not have been initialized properly.");
            Console.exception(e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Console.exception(e);
        }
        return null;
    }

    public static byte[] encryptMessage(byte[] myBytes) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sPartnerPublicKey);
            return cipher.doFinal(myBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Console.d("This machine does not support RSA encryption methods.");
        } catch (InvalidKeyException e) {
            Console.d("Partner public key unknown");
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Console.exception(e);
        }
        return null;
    }

    public static byte[] signMessage(byte[] message) {
        try {
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(getPrivateKey());
            privateSignature.update(message);

            return privateSignature.sign();
        } catch (Exception e) {
            Console.exception(e);
            return null;
        }
    }

    public static boolean verifySignature(byte[] message, byte[] signature) {
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(getPartnerPublicKey());
            publicSignature.update(message);

            return publicSignature.verify(signature);
        } catch (Exception e) {
            Console.exception(e);
            return false;
        }
    }

    private static void generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE, new SecureRandom());

            sKeyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            Console.d("This machine does not support RSA encryption methods.");
        }
    }
}
