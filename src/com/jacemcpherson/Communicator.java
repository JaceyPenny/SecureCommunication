package com.jacemcpherson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Communicator {

    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 9090;

    private static Communicator sCommunicator;

    private static Socket sConnectedSocket;

    public static Communicator startServer() {
        if (sCommunicator != null && !sCommunicator.isServer()) {
            throw new RuntimeException("Cannot initialize server: Already initialized as client.");
        } else if (sCommunicator != null) {
            return sCommunicator;
        }

        try {
            sCommunicator = new Communicator(true);
        } catch (IOException e) {
            Console.d("Cannot initialize server: Exception occurred.");
            Console.exception(e);
            return null;
        }

        return sCommunicator;
    }

    public static Communicator startClient() {
        if (sCommunicator != null && sCommunicator.isServer()) {
            throw new RuntimeException("Cannot initialize client: Already initialized as server.");
        } else if (sCommunicator != null) {
            return sCommunicator;
        }

        if (!isServerOpen()) {
            Console.d("Cannot initialize client: Server is not open.");
            return null;
        }

        try {
            sCommunicator = new Communicator(false);
        } catch (IOException e) {
            Console.d("Cannot initialize client: Exception occurred.");
            Console.exception(e);
        }

        return sCommunicator;
    }

    public static boolean isServerOpen() {
        try {
            if (sConnectedSocket != null && sConnectedSocket.isConnected()) {
                return true;
            }
            Socket connectedSocket = new Socket(SERVER_IP, SERVER_PORT);
            // server connected
            sConnectedSocket = connectedSocket;
            return true;
        } catch (IOException e) {
            // server did not connect
            return false;
        }
    }


    private ServerSocket mServerSocket;
    private Socket mSocket;

    private BufferedReader mSocketReader;
    private PrintWriter mSocketWriter;

    private boolean mIsServer;

    public Communicator(boolean isServer) throws IOException {
        mIsServer = isServer;
        if (isServer) {
            mServerSocket = new ServerSocket(SERVER_PORT);
        } else {
            mSocket = sConnectedSocket != null ? sConnectedSocket : new Socket(SERVER_IP, SERVER_PORT);
        }
    }

    public void waitForConnection() {
        if (isServer()) {
            try {
                mSocket = mServerSocket.accept();
            } catch (IOException e) {
                Console.d("Could not accept connection.");
                Console.exception(e);
                mSocket = null;
            }
        } else {
            Console.w("Could not wait for connection: Not the server");
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void exchangePublicKeys() {

    }

    public void close() {
        try {
            if (mSocket != null) {
                mSocket.close();
            }

            if (isServer()) {
                mServerSocket.close();
            }
        } catch (IOException e) {

        }
    }

    public boolean isServer() {
        return mIsServer;
    }

    public boolean isConnected() {
        return mSocket != null;
    }

    public boolean isConnectedServer() {
        return isServer() && isConnected();
    }

    public void sendBytes(byte[] bytes) {
        if (bytes == null) {
            Console.d("bytes are null.");
            return;
        }

        if (isConnected()) {
            try {
                String lengthString = getLengthString(bytes.length);
                mSocket.getOutputStream().write(lengthString.getBytes());
                mSocket.getOutputStream().write(bytes);
                Console.d("Sent: %s", new String(bytes));
            } catch (Exception e) {
                Console.exception(e);
            }
        } else {
            Console.d("Could not send data: Not connected.");
        }
    }

    private static String getLengthString(int length) {
        return StringUtil.padded(length, 8);
    }

    public byte[] receiveBytes() {
        if (isConnected()) {
            try {
                byte[] lengthInformation = new byte[8];
                mSocket.getInputStream().read(lengthInformation, 0, 8);

                int length = Integer.parseInt(new String(lengthInformation).trim());

                byte[] reading = new byte[length];
                int receivedLength = mSocket.getInputStream().read(reading, 0, length);

                if (receivedLength != length) {
                    Console.d("Lengths are different");
                }

                return reading;
            } catch (IOException | NumberFormatException e) {
                Console.exception(e);
                return null;
            }
        }
        return null;
    }
}
