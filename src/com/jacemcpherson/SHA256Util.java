package com.jacemcpherson;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.util.Arrays;

public class SHA256Util {

    /**
     * Performs a hash of the message using SHA-256 with a secret key (which is stored in {@link AESEncryptionUtil}).
     * @param message
     * @return
     */
    public static byte[] getHMAC(byte[] message) {
        try {
            SecretKey secretKey = AESEncryptionUtil.getSecretKey();

            Mac sha256Mac = Mac.getInstance("HmacSHA256");
            sha256Mac.init(secretKey);

            return sha256Mac.doFinal(message);
        } catch (Exception e) {
            Console.exception(e);
            return null;
        }
    }

    /**
     * Compares two byte arrays for equality.
     * @param message1
     * @param message2
     * @return whether message1 and message2 are equal.
     */
    public static boolean messagesEqual(byte[] message1, byte[] message2) {
        return Arrays.equals(message1, message2);
    }
}
