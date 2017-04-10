package com.jacemcpherson;

import java.util.Base64;

public class Main {

    public static void main(String[] args) {
        Console.init();

        Communicator communicator = null;

        while (communicator == null) {
            boolean isServerOpen = Communicator.isServerOpen();

            if (isServerOpen) {
                Console.d("Other machine is server. Connecting...");

                communicator = Communicator.startClient();

                Console.d("Connected to " + communicator.getSocket().getInetAddress().toString() + ":" + communicator.getSocket().getPort());
            } else {
                Console.d("Server is not open. Are you the server (y/n)? ");
                String input = Console.getLine();
                if (input.toLowerCase().startsWith("y")) {
                    Console.d("Starting server...");
                    communicator = Communicator.startServer();

                    if (communicator == null) {
                        Console.d("Could not initialize server. Exiting...");
                        return;
                    }

                    Console.d("Waiting for client connection.... ");
                    communicator.waitForConnection();
                    Console.d("Connected to " + communicator.getSocket().getInetAddress().toString() + ":" + communicator.getSocket().getPort());
                }
            }
        }

        // Exchange public keys
        communicator.exchangeRSAPublicKey();

        // Step 1: Setup a shared secret key
        Console.d(StringUtil.repeatedCharacter('=', 40));
        Console.d("*** STEP 1 ***");

        communicator.exchangeSecretKey();

        Console.d(StringUtil.repeatedCharacter('=', 40));
        Console.d("*** STEP 2 ***");

        if (!communicator.isServer()) {  // I'm Alice
            // Step 2: Alice sends AES encrypted message (30 bytes)
            byte[] message = AESEncryptionUtil.generateRandomMessage(30);
            Console.d("Alice will send (unencrypted): %s", Base64.getEncoder().encodeToString(message));

            byte[] encryptedMessage = AESEncryptionUtil.encryptMessage(message);
            communicator.sendBytes(encryptedMessage);

        } else {    // I'm Bob
            // Step 2: Alice sends AEX
            byte[] encryptedMessage = communicator.receiveBytes();
            byte[] decryptedMessage = AESEncryptionUtil.decryptMessage(encryptedMessage);

            Console.d("Bob received (unencrypted): %s", Base64.getEncoder().encodeToString(decryptedMessage));
        }

        Console.d(StringUtil.repeatedCharacter('=', 40));
        Console.d("*** STEP 3 ***");

        if (!communicator.isServer()) {
            // Step 3: Alice sends a 40 byte message, followed by its HMAC.

            byte[] message = AESEncryptionUtil.generateRandomMessage(40);
            Console.d("Alice will send message: %s", Base64.getEncoder().encodeToString(message));

            byte[] messageHMAC = SHA256Util.getHMAC(message);
            Console.d("Alice computed HMAC: %s", Base64.getEncoder().encodeToString(messageHMAC));

            communicator.sendBytes(message);
            communicator.sendBytes(messageHMAC);
        } else {
            byte[] receivedMessage = communicator.receiveBytes();
            byte[] receivedHMAC = communicator.receiveBytes();

            byte[] computedHMAC = SHA256Util.getHMAC(receivedMessage);

            boolean hashesMatch = SHA256Util.messagesEqual(receivedHMAC, computedHMAC);

            Console.d("Bob received message: %s", Base64.getEncoder().encodeToString(receivedMessage));
            Console.d("Bob received HMAC: %s", StringUtil.bytesToHex(receivedHMAC));
            Console.d("Bob computed HMAC: %s", StringUtil.bytesToHex(computedHMAC));
            Console.d("Messages are %s", hashesMatch ? "THE SAME" : "NOT THE SAME");
        }

        Console.d(StringUtil.repeatedCharacter('=', 40));
        Console.d("*** STEP 4 ***");

        if (!communicator.isServer()) {

        } else {

        }

        communicator.close();

    }
}
