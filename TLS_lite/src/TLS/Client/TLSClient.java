package TLS.Client;

import TLS.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;

public class TLSClient {
    private static final String host_ = "localhost";
    private static final int port_ = 4433;

    public static void main(String[] args) throws Exception {
        // Load client credentials
        X509Certificate clientCert = KeyLoader.loadCertificate("src/certs/CASignedClientCertificate.pem");
        PrivateKey clientKey = KeyLoader.loadPrivateKey("src/certs/clientPrivateKey.der");

        System.out.println("Connecting to server...");
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            System.out.println("[CLIENT] Creating socket to " + host_ + ":" + port_);
            socket = new Socket(host_, port_);

            // Important: Create output stream BEFORE input stream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // Flush header information

            in = new ObjectInputStream(socket.getInputStream());

            // 1. Initiate the handshake
            TLSHandshake clientHandshake = new TLSHandshake();
            HandshakeMessages.ClientHello hello = clientHandshake.initiateHandshake();
            out.writeObject(hello);
            out.flush();

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new ObjectOutputStream(baos).writeObject(hello);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 2. Receive server hello
            HandshakeMessages.ServerHello serverHello = (HandshakeMessages.ServerHello) in.readObject();
            clientHandshake.handshakeMessages_.add(serverHello);

            // 3. Send client response
            HandshakeMessages.ClientResponse clientResponse = clientHandshake.respondToServerHello(serverHello, (RSAPrivateKey) clientKey, clientCert);
            out.writeObject(clientResponse);
            out.flush();

            // 4. Compute session keys
            TLSHandshake.SecretKeys keys = clientHandshake.computeSessionKeys(clientHandshake.getClientDHPrivateKey(), serverHello.serverDHPub_, hello.nonce_);

            // 5. Receive server finished message (which contains server's HMAC)
            HandshakeMessages.ServerFinished serverFin = (HandshakeMessages.ServerFinished) in.readObject();
            clientHandshake.handshakeMessages_.add(serverFin);

            // Generate expected server HMAC for verification
            byte[] expectedServerHmac = HandshakeUtil.generateFinishedMessage(
                    hello.nonce_,
                    serverHello.serverCert_,
                    serverHello.serverDHPub_,
                    serverHello.signedServerDHPub_,
                    clientResponse.clientCert_,
                    clientResponse.clientDHPub_,
                    clientResponse.signedClientDHPub_,
                    keys.serverMacKey_,
                    true // Server HMAC
            );

            if (!MessageDigest.isEqual(expectedServerHmac, serverFin.serverHmac_)) {
                throw new SecurityException("Server HMAC verification failed");
            }

            // 6. Generate client's HMAC and send client finished message
            byte[] clientHmac = HandshakeUtil.generateClientFinished(
                    hello.nonce_,
                    serverHello.serverCert_,
                    serverHello.serverDHPub_,
                    serverHello.signedServerDHPub_,
                    clientResponse.clientCert_,
                    clientResponse.clientDHPub_,
                    clientResponse.signedClientDHPub_,
                    serverFin.serverHmac_,
                    keys.clientMacKey_
            );

            HandshakeMessages.ClientFinished clientFinished = new HandshakeMessages.ClientFinished(clientHmac);
            clientHandshake.handshakeMessages_.add(clientFinished);
            out.writeObject(clientFinished);
            out.flush();

            System.out.println("[CLIENT] Handshake complete! Starting secure communication...");

            // 7. Secure communication
            MessageCrypto crypto = new MessageCrypto(keys.clientEncKey_, keys.clientMacKey_, keys.clientIV_);

            try {
                // Send a message to the server
                String message = "Hello Server!";

                byte[] encrypted = crypto.encrypt(message);
                out.writeObject(encrypted);
                out.flush();

                // Wait for and process server response with timeout
                socket.setSoTimeout(10000); // 10 second timeout

                try {
                    Object obj = in.readObject();

                    if (obj instanceof byte[]) {
                        byte[] encryptedResponse = (byte[]) obj;

                        Object decrypted = crypto.decrypt(encryptedResponse);

                        if (decrypted instanceof String) {
                            String response = (String) decrypted;
                        } else {
                            System.out.println("[CLIENT] Received non-string response: " +
                                    (decrypted != null ? decrypted.getClass().getName() : "null"));
                        }
                    } else {
                        System.out.println("[CLIENT] Error: Expected byte array but got: " + obj.getClass().getName());
                    }
                } catch (SocketTimeoutException ste) {
                    System.out.println("[CLIENT] Timeout waiting for server response");
                    System.out.println("[CLIENT] Last socket state - Connected: " + socket.isConnected() +
                            ", Closed: " + socket.isClosed() +
                            ", Input shutdown: " + socket.isInputShutdown() +
                            ", Output shutdown: " + socket.isOutputShutdown());
                } catch (EOFException eof) {
                    System.out.println("[CLIENT] EOFException: Server might have closed the connection");
                    System.out.println("[CLIENT] Last socket state - Connected: " + socket.isConnected() +
                            ", Closed: " + socket.isClosed() +
                            ", Input shutdown: " + socket.isInputShutdown() +
                            ", Output shutdown: " + socket.isOutputShutdown());
                }
            } catch (Exception e) {
                System.err.println("[CLIENT] Error in secure communication:");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("[CLIENT] Exception in client:");
            e.printStackTrace();
        } finally {
            // Close streams and socket
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[CLIENT] Resources cleaned up");
        }
    }
}