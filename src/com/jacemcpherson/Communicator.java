package com.jacemcpherson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

/**
 * Provides a wrapper around Server/Client socket programming, simplifying the process of initializing/connecting
 * to a server.
 * <br><br>
 * Users of this class should initialize a server using {@link #startServer()}. In a separate runtime, you
 * can connect to this running server using {@link #startClient()}.
 * <br><br>
 * Using {@link #sendBytes(byte[])} and {@link #receiveBytes()} you can easily communicate byte streams between
 * server and client over a single socket.
 * <br><br>
 * A level of security is added when users exchange RSA public keys (using {@link #exchangeRSAPublicKey()}, and
 * subsequently {@link #exchangeSecretKey()} for message encryption using AES or other ciphers.
 */
public class Communicator {

    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 9090;

    private static Communicator sCommunicator;

    private static Socket sConnectedSocket;

    /**
     * Starts a server on {@link #SERVER_IP}:{@link #SERVER_PORT} (defaults to localhost:9090)
     * @return the resulting {@link Communicator}
     */
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

    /**
     * Starts a client (assuming a server is already running), connecting to {@link #SERVER_IP}:{@link #SERVER_PORT}
     * @return the resulting {@link Communicator}
     */
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

    /**
     * Tests if the server is open by creating a {@link Socket} and verifying that connection was successful.
     * If connection succeeded, we hold onto that Socket, rather than closing the connection. This is in case the
     * user wants to call {@link #startClient()}, then we do not have to reconnect to the server.
     * @return <code>true</code> if the server is up on {@link #SERVER_IP}:{@link #SERVER_PORT}, <code>false</code> otherwise.
     */
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

    private boolean mIsServer;

    /**
     * Constructs a new Communicator.
     * @param isServer whether this communicator acts as the server or as a client
     * @throws IOException if socket creation/connection is unsuccessful.
     */
    public Communicator(boolean isServer) throws IOException {
        mIsServer = isServer;
        if (isServer) {
            mServerSocket = new ServerSocket(SERVER_PORT);
        } else {
            mSocket = sConnectedSocket != null ? sConnectedSocket : new Socket(SERVER_IP, SERVER_PORT);
        }
    }

    /**
     * Blocks the current thread until the server accepts a connection from a client.
     */
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

    /**
     * Performs the exchange of RSA Public keys between a server and client pair.
     */
    public void exchangeRSAPublicKey() {
        // server will send first
        if (isServer()) {
            Console.d("Bob is sending his public key");
            sendBytes(RSAEncryptionUtil.getPublicKeyEncoded());
            Console.d("Bob is receiving Alice's public key");
            byte[] otherPublic = receiveBytes();
            RSAEncryptionUtil.decodePublicKey(otherPublic);
        } else {
            Console.d("Alice is receiving Bob's public key");
            byte[] otherPublic = receiveBytes();
            RSAEncryptionUtil.decodePublicKey(otherPublic);
            Console.d("Alice is sending her public key");
            sendBytes(RSAEncryptionUtil.getPublicKeyEncoded());
        }
    }

    /**
     * Performs the transfer of a secret key from the client to the server (i.e. server receives a generated
     * key from the client).
     */
    public void exchangeSecretKey() {
        // Bob (server) will receive the secret key, decrypt
        if (isServer()) {
            byte[] encryptedKey = receiveBytes();
            byte[] secretKeyEncoded = RSAEncryptionUtil.decryptMessage(encryptedKey);

            String receivedKey = Base64.getEncoder().encodeToString(secretKeyEncoded);
            Console.d("Bob received secret key: %s", receivedKey);

            AESEncryptionUtil.decodeSecretKey(secretKeyEncoded);
        } else { // Alice (client) will generate and send her secret key
            byte[] secretKeyEncoded = AESEncryptionUtil.getSecretKeyEncoded();
            byte[] encryptedKey = RSAEncryptionUtil.encryptMessage(secretKeyEncoded);

            String sentKey = Base64.getEncoder().encodeToString(secretKeyEncoded);
            Console.d("Alice sent secret key: %s", sentKey);

            sendBytes(encryptedKey);
        }
    }

    /**
     * Closes the connections made by this Communicator.
     */
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

    /**
     * Sends the contents of "bytes" to the other party. The length of the stream is encoded as a String
     * and padded to 8 characters. The bytes array is sent directly after those 8 bytes.
     * @param bytes the data to send to the other party
     */
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
//                Console.d("Sent: %s", Base64.getEncoder().encodeToString(bytes));
            } catch (Exception e) {
                Console.exception(e);
            }
        } else {
            Console.d("Could not send data: Not connected.");
        }
    }

    /**
     * Gets the String value for an int, then pads that String to 8 characters.
     * @param length
     * @return a String with length 8
     */
    private static String getLengthString(int length) {
        return StringUtil.padded(length, 8);
    }

    /**
     * Receives a byte stream sent by the other party. The other party sends the first 8 bytes as the length
     * of the message, encoded as a String. On the receiving end, we decode this String to an integer, then
     * receive exactly that many bytes from the Socket InputStream.
     * @return
     */
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
