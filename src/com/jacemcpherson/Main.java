package com.jacemcpherson;

public class Main {

    public static void main(String[] args) {
        Console.init();

        Communicator communicator = null;

        while (communicator == null) {
            boolean isServerOpen = Communicator.isServerOpen();

            if (isServerOpen) {
                Console.d("Other machine is server. Connecting...");

                communicator = Communicator.startClient();

                Console.d("Connected to " + communicator.getSocket().getInetAddress().toString());
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
                    Console.d("Connected to " + communicator.getSocket().getInetAddress().toString());
                }
            }
        }

        communicator.waitForConnection();
        communicator.sendBytes("Testing".getBytes());
        byte[] result = communicator.receiveBytes();


        if (result != null) {
            Console.d(new String(result));
        } else {
            Console.d("Something went wrong...");
        }

    }
}
