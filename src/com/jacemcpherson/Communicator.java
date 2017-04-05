package com.jacemcpherson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Communicator {

    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 2800;

    private static Communicator sCommunicator;

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
            Socket newSocket = new Socket(SERVER_IP, SERVER_PORT);
            // server successfully connected
            newSocket.close();
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

            Thread socketConnectionThread = new Thread(() -> {
                try {
                    mSocket = mServerSocket.accept();
                    setupBuffers();
                } catch (IOException e) {
                    Console.d("Could not accept connection.");
                    mSocket = null;
                }
            });
            socketConnectionThread.start();
        } else {
            mSocket = new Socket(SERVER_IP, SERVER_PORT);
            setupBuffers();
        }
    }

    public void waitForConnection() {
        if (isServer()) {
            while (mSocket == null) {
                // do nothing...
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void exchangePublicKeys() {

    }

    public void setupBuffers() {
        try {
            Console.d("Setting up buffers");
            mSocketReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mSocketWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
        } catch (IOException e) {
            Console.exception(e);
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

                mSocketWriter.println(bytes.length);
                mSocket.getOutputStream().write(bytes);
            } catch (IOException e) {
                Console.exception(e);
            }
        } else {
            Console.d("Could not send data: Not connected.");
        }
    }

    public byte[] receiveBytes() {
        if (isConnected()) {
            try {
                int length = Integer.parseInt(mSocketReader.readLine());
                byte[] buffer = new byte[length];
                int received = mSocket.getInputStream().read(buffer, 0, length);
                if (received != length) {
                    throw new RuntimeException("Error: Received fewer bytes than expected");
                }
                return buffer;
            } catch (IOException | NumberFormatException e) {
                Console.exception(e);
                return null;
            }
        }
        return null;
    }
}
