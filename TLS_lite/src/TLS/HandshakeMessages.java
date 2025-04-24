package TLS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.X509Certificate;

public class HandshakeMessages {
    // Client's first message - just sends a nonce
    public static class ClientHello implements Serializable {
        public final byte[] nonce_;

        public ClientHello(byte[] nonce) {
            nonce_ = nonce;
        }
    }

    // Server's response - sends certificate, Diffie-Hellman public key, and signed Diffie-Hellman key
    public static class ServerHello implements Serializable {
        public final X509Certificate serverCert_;
        public final BigInteger serverDHPub_;
        public final byte[] signedServerDHPub_;

        public ServerHello(X509Certificate serverCertificate, BigInteger serverDHPublicKey, byte[] signedServerDHPubKey) {
            serverCert_ = serverCertificate;
            serverDHPub_ = serverDHPublicKey;
            signedServerDHPub_ = signedServerDHPubKey;
        }
    }

    // Client's response - sends certificate, Diffie-Hellman public key, and signed Diffie-Hellman key
    public static class ClientResponse implements Serializable {
        public final X509Certificate clientCert_;
        public final BigInteger clientDHPub_;
        public final byte[] signedClientDHPub_;

        public ClientResponse(X509Certificate clientCertificate, BigInteger clientDHPublicKey, byte[] signedClientDHPubKey) {
            clientCert_ = clientCertificate;
            clientDHPub_ = clientDHPublicKey;
            signedClientDHPub_ = signedClientDHPubKey;
        }
    }

    // Final verification messages
    public static class ServerFinished implements Serializable {
        public final byte[] serverHmac_;

        public ServerFinished(byte[] hmac) {
            serverHmac_ = hmac;
        }
    }

    public static class ClientFinished implements Serializable {
        public final byte[] clientHmac_;

        public ClientFinished(byte[] hmac) {
            clientHmac_ = hmac;
        }
    }

    public static class HandshakeTranscript implements Serializable {
        private ByteArrayOutputStream transcript = new ByteArrayOutputStream();

        // Add raw bytes to the transcript
        public synchronized void addBytes(byte[] data) {
            try {
                transcript.write(data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write to transcript", e);
            }
        }
    }
}