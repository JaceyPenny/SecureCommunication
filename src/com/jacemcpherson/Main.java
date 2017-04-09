package com.jacemcpherson;

public class Main {

    public static void main(String[] args) {
        Console.init();

        Communicator communicator = null;

        while (communicator == null) {
            boolean isServerOpen = Communicator.isServerOpen();
//            boolean isServerOpen = true;

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

        // Testing Communication TODO: Remove this section
        if (communicator.isServer()) {
            Console.d("Sending client a hello message...");
            communicator.sendBytes("Hello, Alice".getBytes());

            Console.d("Waiting for response...");
            byte[] array = communicator.receiveBytes();
            Console.d("Bytes received");
            String received = new String(array);
            Console.d(received);
        } else {
            Console.d("Waiting for hello from Bob...");
            byte[] array = communicator.receiveBytes();
            Console.d("Received: \"" + new String(array) + "\"");

            Console.d("Responding to server...");
            communicator.sendBytes("Testing".getBytes());
        }
        // Testing Communication TODO: Remove this section

        

        communicator.close();

    }
}
