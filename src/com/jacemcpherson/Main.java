package com.jacemcpherson;

import java.util.Base64;

public class Main {

    public static void main(String[] args) {
        Console.init();

        // the Communicator for this party (doesn't matter if it's server or client)
        Communicator communicator = null;

        // As long as no server is up, we continue to ask the user if they're the server.
        while (communicator == null) {
            boolean isServerOpen = Communicator.isServerOpen();

            if (isServerOpen) { // server is up
                Console.d("Other machine is server. Connecting...");

                communicator = Communicator.startClient();  // because other machine is server, we are client

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
            byte[] encryptedMessage = AESEncryptionUtil.encryptMessage(message);

            Console.d("Alice will send (unencrypted): %s", Base64.getEncoder().encodeToString(message));

            communicator.sendBytes(encryptedMessage);
        } else {    // I'm Bob
            // Step 2: Alice sends AEX
            byte[] encryptedMessage = communicator.receiveBytes();
            byte[] decryptedMessage = AESEncryptionUtil.decryptMessage(encryptedMessage);

            Console.d("Bob received (unencrypted): %s", Base64.getEncoder().encodeToString(decryptedMessage));
        }

        Console.d(StringUtil.repeatedCharacter('=', 40));
        Console.d("*** STEP 3 ***");

        if (!communicator.isServer()) { // I'm Alice
            // Step 3: Alice sends a 40 byte message, followed by its HMAC.

            byte[] message = AESEncryptionUtil.generateRandomMessage(40);
            byte[] messageHMAC = SHA256Util.getHMAC(message);

            Console.d("Alice will send message: %s", Base64.getEncoder().encodeToString(message));
            Console.d("Alice computed HMAC: %s", StringUtil.bytesToHex(messageHMAC));

            communicator.sendBytes(message);
            communicator.sendBytes(messageHMAC);
        } else {    // I'm Bob
            byte[] receivedMessage = communicator.receiveBytes();
            byte[] receivedHMAC = communicator.receiveBytes();

            byte[] computedHMAC = SHA256Util.getHMAC(receivedMessage);

            boolean hashesMatch = SHA256Util.messagesEqual(receivedHMAC, computedHMAC);

            Console.d("Bob received message: %s", Base64.getEncoder().encodeToString(receivedMessage));
            Console.d("Bob received HMAC: %s", StringUtil.bytesToHex(receivedHMAC));
            Console.d("Bob computed HMAC: %s", StringUtil.bytesToHex(computedHMAC));
            Console.d("Bob determined the message was %s", hashesMatch ? "NOT MODIFIED" : "MODIFIED");
        }

        Console.d(StringUtil.repeatedCharacter('=', 40));
        Console.d("*** STEP 4 ***");

        if (!communicator.isServer()) { // I'm Alice
            byte[] message = AESEncryptionUtil.generateRandomMessage(50);
            byte[] hmac = SHA256Util.getHMAC(message);
            byte[] signature = RSAEncryptionUtil.signMessage(hmac);

            Console.d("Alice will send message: %s", Base64.getEncoder().encodeToString(message));
            Console.d("Alice computed HMAC: %s", StringUtil.bytesToHex(hmac));
            Console.d("Alice signed: %s", Base64.getEncoder().encodeToString(signature));

            communicator.sendBytes(message);
            communicator.sendBytes(hmac);
            communicator.sendBytes(signature);
        } else {    // I'm Bob
            byte[] receivedMessage = communicator.receiveBytes();
            byte[] receivedHmac = communicator.receiveBytes();
            byte[] receivedSignature = communicator.receiveBytes();

            byte[] computedHmac = SHA256Util.getHMAC(receivedMessage);

            boolean hashesMatch = SHA256Util.messagesEqual(receivedHmac, computedHmac);
            boolean signatureValid = RSAEncryptionUtil.verifySignature(receivedHmac, receivedSignature);

            Console.d("Bob received message: %s", Base64.getEncoder().encodeToString(receivedMessage));
            Console.d("Bob received HMAC: %s", StringUtil.bytesToHex(receivedHmac));
            Console.d("Bob computed HMAC: %s", StringUtil.bytesToHex(computedHmac));
            Console.d("Bob determined the message was %s", hashesMatch ? "NOT MODIFIED" : "MODIFIED");
            Console.d("Bob received signature: %s", Base64.getEncoder().encodeToString(receivedSignature));
            Console.d("Bob determined the signature is %s", signatureValid ? "VALID" : "NOT VALID");
        }

        Console.d(StringUtil.repeatedCharacter('=', 40));
        Console.d("*** CLOSING ***");

        communicator.close();

    }
}
