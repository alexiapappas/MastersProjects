package TLS.Server;

import TLS.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;

public class TLSServer {
    private static final int port_ = 4433;

    public static void main(String[] args) throws IOException {
        try {
            // Load server credentials
            X509Certificate serverCert = KeyLoader.loadCertificate("src/certs/CASignedServerCertificate.pem");
            PrivateKey serverKey = KeyLoader.loadPrivateKey("src/certs/serverPrivateKey.der");

            System.out.println("Starting server on port: " + port_);
            try (ServerSocket serverSocket = new ServerSocket(port_)) {
                while (true) {
                    new ClientHandler(serverSocket.accept(), serverCert, serverKey).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket_;
        private final X509Certificate serverCert_;
        private final PrivateKey serverKey_;

        public ClientHandler(Socket socket, X509Certificate cert, PrivateKey key) {
            socket_ = socket;
            serverCert_ = cert;
            serverKey_ = key;
        }

        public void run() {
            ObjectOutputStream out = null;
            ObjectInputStream in = null;

            try {
                // Create output stream BEFORE input stream
                out = new ObjectOutputStream(socket_.getOutputStream());
                out.flush(); // Flush header information

                in = new ObjectInputStream(socket_.getInputStream());

                // 1. Receive client hello
                HandshakeMessages.ClientHello cHello = (HandshakeMessages.ClientHello) in.readObject();
                TLSHandshake serverHandshake = new TLSHandshake();
                serverHandshake.handshakeMessages_.add(cHello);

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    new ObjectOutputStream(baos).writeObject(cHello);
                    byte[] hash = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 2. Send server hello
                HandshakeMessages.ServerHello sHello = serverHandshake.respondToClientHello((RSAPrivateKey) serverKey_, serverCert_);
                out.writeObject(sHello);
                out.flush();

                // 3. Receive client response
                HandshakeMessages.ClientResponse cResponse = (HandshakeMessages.ClientResponse) in.readObject();
                serverHandshake.verifyClientResponse(cResponse);
                serverHandshake.handshakeMessages_.add(cResponse);

                // 4. Compute session keys
                TLSHandshake.SecretKeys keys = serverHandshake.computeSessionKeys(serverHandshake.getServerDHPrivateKey(), cResponse.clientDHPub_, cHello.nonce_);

                // 5. Generate and send server finished message with HMAC
                byte[] serverHmac = HandshakeUtil.generateFinishedMessage(
                        cHello.nonce_,
                        sHello.serverCert_,
                        sHello.serverDHPub_,
                        sHello.signedServerDHPub_,
                        cResponse.clientCert_,
                        cResponse.clientDHPub_,
                        cResponse.signedClientDHPub_,
                        keys.serverMacKey_,
                        true // Server HMAC
                );

                HandshakeMessages.ServerFinished serverFinished = new HandshakeMessages.ServerFinished(serverHmac);
                serverHandshake.handshakeMessages_.add(serverFinished);

                out.writeObject(serverFinished);
                out.flush();

                // 6. Receive and verify client finished message
                HandshakeMessages.ClientFinished clientFin = (HandshakeMessages.ClientFinished) in.readObject();
                serverHandshake.handshakeMessages_.add(clientFin);

                // Generate expected client HMAC for verification
                byte[] expectedClientHmac = HandshakeUtil.generateClientFinished(
                        cHello.nonce_,
                        sHello.serverCert_,
                        sHello.serverDHPub_,
                        sHello.signedServerDHPub_,
                        cResponse.clientCert_,
                        cResponse.clientDHPub_,
                        cResponse.signedClientDHPub_,
                        serverHmac,
                        keys.clientMacKey_
                );

                if (!MessageDigest.isEqual(expectedClientHmac, clientFin.clientHmac_)) {
                    throw new SecurityException("Client HMAC verification failed");
                }

                System.out.println("[SERVER] Handshake complete! Secure channel established.");

                // 7. Secure communication
                MessageCrypto crypto = new MessageCrypto(keys.serverEncKey_, keys.serverMacKey_, keys.serverIV_);

                try {
                    // Set timeout for reading client message
                    socket_.setSoTimeout(10000); // 10 seconds

                    // Receive client message
                    Object obj = in.readObject();

                    if (obj instanceof byte[]) {
                        byte[] encryptedMessage = (byte[]) obj;

                        // Decrypt and process
                        Object decrypted = crypto.decrypt(encryptedMessage);
                        if (decrypted instanceof String) {
                            String message = (String) decrypted;

                            // Send response
                            String response = "Hello client! Your message was: " + message;

                            byte[] encryptedResponse = crypto.encrypt(response);

                            out.writeObject(encryptedResponse);
                            out.flush();

                            // Add a delay to ensure client reads response
                            Thread.sleep(3000);
                        } else {
                            System.out.println("[SERVER] Received non-string message");
                        }
                    } else {
                        System.out.println("[SERVER] Error: Expected byte array but got: " + obj.getClass().getName());
                    }
                } catch (Exception e) {
                    System.err.println("[SERVER] Error in secure communication:");
                    e.printStackTrace();
                }
                System.out.println("[SERVER] Handler complete, closing connection");
            } catch (Exception e) {
                System.err.println("[SERVER] Exception in client handler:");
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
                    socket_.close();
                } catch (IOException e) {
                    System.err.println("[SERVER] Error closing resources:");
                    e.printStackTrace();
                }
                System.out.println("[SERVER] Resources cleaned up");
            }
        }
    }
}