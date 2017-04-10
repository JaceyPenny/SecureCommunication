package com.jacemcpherson;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.util.Arrays;

public class SHA256Util {

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

    public static boolean messagesEqual(byte[] message1, byte[] message2) {
        return Arrays.equals(message1, message2);
    }
}
