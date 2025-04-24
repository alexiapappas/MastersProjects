package TLS;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TLSHandshake {
    private BigInteger clientDHPrivateKey_;
    private BigInteger serverDHPrivateKey_;
    private final SecureRandom random_ = new SecureRandom();
    public final List<Object> handshakeMessages_ = new ArrayList<>();
    private final HandshakeMessages.HandshakeTranscript transcript_ = new HandshakeMessages.HandshakeTranscript();
    private static final PublicKey CA_PublicKey_ = loadCAPublicKey();

    // Diffie-Hellman parameters (should be agreed upon by client and server)
    public static final BigInteger diffiehellmanG_ = new BigInteger("2");
    public static final BigInteger diffiehellmanP_ = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                    "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                    "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                    "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                    "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                    "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                    "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                    "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                    "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                    "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);

    private static PublicKey loadCAPublicKey() {
        try {
            // Load CA Certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            FileInputStream fis = new FileInputStream("src/certs/CAcertificate.pem");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(fis);
            fis.close();

            // Extract public key
            return caCert.getPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CA public key",e);
        }
    }

    public TLSHandshake() {}

    // Client initiates handshake by generating a random nonce
    public HandshakeMessages.ClientHello initiateHandshake() {
        byte[] nonce = new byte[32];
        random_.nextBytes(nonce);

        // Generate and store client Diffie-Hellman private key
        this.clientDHPrivateKey_ = new BigInteger(2048, random_);

        HandshakeMessages.ClientHello hello = new HandshakeMessages.ClientHello(nonce);
        handshakeMessages_.add(hello);

        // Add to transcript
        transcript_.addBytes(nonce);

        return hello;
    }

    // Server responds to client hello with certificate and signed Diffie-Hellman parameters
    public HandshakeMessages.ServerHello respondToClientHello(RSAPrivateKey serverPrivateKey, X509Certificate serverCert) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, CertificateEncodingException {
        // Generate Diffie-Hellman key pair
        this.serverDHPrivateKey_ = new BigInteger(2048, random_);
        BigInteger serverDHPub = diffiehellmanG_.modPow(serverDHPrivateKey_, diffiehellmanP_);

        // Sign Diffie-Hellman public key with server's RSA key
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(serverPrivateKey);
        signer.update(serverDHPub.toByteArray());
        byte[] signedDHPub = signer.sign();

        HandshakeMessages.ServerHello response = new HandshakeMessages.ServerHello(serverCert, serverDHPub, signedDHPub);
        handshakeMessages_.add(response);

        // Add to transcript
        transcript_.addBytes(serverCert.getEncoded());
        transcript_.addBytes(serverDHPub.toByteArray());
        transcript_.addBytes(signedDHPub);

        return response;
    }

    // Client verifies server's response and sends its own credentials
    public HandshakeMessages.ClientResponse respondToServerHello(HandshakeMessages.ServerHello hello, RSAPrivateKey clientPrivateKey, X509Certificate clientCert) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, CertificateException, NoSuchProviderException {
        // Verify server's certificate and signature
        hello.serverCert_.verify(CA_PublicKey_);

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(hello.serverCert_.getPublicKey());
        verifier.update(hello.serverDHPub_.toByteArray());
        if (!verifier.verify(hello.signedServerDHPub_))
            throw new SecurityException("Invalid server DH signature");

        // Generate client Diffie-Hellman key pair
        BigInteger clientDHPub = diffiehellmanG_.modPow(clientDHPrivateKey_, diffiehellmanP_);

        // Sign Diffie-Hellman public key
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(clientPrivateKey);
        signer.update(clientDHPub.toByteArray());
        byte[] signedDHPub = signer.sign();

        HandshakeMessages.ClientResponse response = new HandshakeMessages.ClientResponse(clientCert, clientDHPub, signedDHPub);
        handshakeMessages_.add(response);

        transcript_.addBytes(clientCert.getEncoded());
        transcript_.addBytes(clientDHPub.toByteArray());
        transcript_.addBytes(signedDHPub);

        return response;
    }

    // Server verifies client response, certificate, and signature
    public void verifyClientResponse(HandshakeMessages.ClientResponse response) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        response.clientCert_.verify(CA_PublicKey_);
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(response.clientCert_.getPublicKey());
        verifier.update(response.clientDHPub_.toByteArray());

        if (!verifier.verify(response.signedClientDHPub_))
            throw new SecurityException("Invalid client DH signature");
    }

    // Compute shared secret and session keys
    public SecretKeys computeSessionKeys(BigInteger myDHPriv, BigInteger otherDHPub, byte[] clientNonce) throws NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {
        HandshakeMessages.ClientHello hello = (HandshakeMessages.ClientHello) handshakeMessages_.get(0);
        clientNonce = hello.nonce_;

        // Compute shared secret
        BigInteger sharedSecret = otherDHPub.modPow(myDHPriv, diffiehellmanP_);
        Mac hmac = Mac.getInstance("HmacSHA256");

        // HKDF Extract
        hmac.init(new SecretKeySpec(clientNonce, "HmacSHA256"));
        byte[] prk = hmac.doFinal(sharedSecret.toByteArray());

        // HKDF Expand
        byte[] serverEnc = hkdfExpand(hmac, prk, "server encrypt");
        byte[] clientEnc = hkdfExpand(hmac, serverEnc, "client encrypt");
        byte[] serverMAC = hkdfExpand(hmac, clientEnc, "server MAC");
        byte[] clientMAC = hkdfExpand(hmac, serverMAC, "client MAC");
        byte[] serverIV = hkdfExpand(hmac, clientMAC, "server IV");
        byte[] clientIV = hkdfExpand(hmac, serverIV, "client IV");


        SecretKeys keys = new SecretKeys(
                new SecretKeySpec(clientMAC, "HmacSHA256"),
                new SecretKeySpec(serverMAC, "HmacSHA256"),
                new SecretKeySpec(clientEnc, "AES"),
                new SecretKeySpec(serverEnc, "AES"),
                clientIV,
                serverIV
        );

        keys.printKeys();

        return keys;
    }

    private byte[] hkdfExpand (Mac hmac, byte[] prev, String label) throws ShortBufferException, InvalidKeyException {
        hmac.init(new SecretKeySpec(prev, "HmacSHA256"));
        return Arrays.copyOf(hmac.doFinal((label + (char)0x01).getBytes()), 16);
    }

    public BigInteger getClientDHPrivateKey() {
        if (clientDHPrivateKey_ == null) {
            throw new IllegalStateException("Client DH private key not generated yet");
        }
        return clientDHPrivateKey_;
    }

    public BigInteger getServerDHPrivateKey() {
        if (serverDHPrivateKey_ == null) {
            throw new IllegalStateException("Server DH private key not generated yet");
        }
        return serverDHPrivateKey_;
    }

    // Helper class to hold all session keys
    public class SecretKeys {
        public final SecretKey clientMacKey_;
        public final SecretKey serverMacKey_;
        public final SecretKey clientEncKey_;
        public final SecretKey serverEncKey_;
        public final byte[] clientIV_;
        public final byte[] serverIV_;

        public SecretKeys (SecretKey clientMacKey, SecretKey serverMacKey, SecretKey clientEncKey, SecretKey serverEncKey, byte[] clientIV, byte[] serverIV) {
            clientMacKey_ = clientMacKey;
            serverMacKey_ = serverMacKey;
            clientEncKey_ = clientEncKey;
            serverEncKey_ = serverEncKey;
            clientIV_ = clientIV;
            serverIV_ = serverIV;
        }

        public void printKeys() {
            System.out.println("Derived keys:");
            System.out.println("Client MAC: " + Arrays.toString(this.clientMacKey_.getEncoded()));
            System.out.println("Server MAC: " + Arrays.toString(this.serverMacKey_.getEncoded()));
            System.out.println("Client IV: " + Arrays.toString(this.clientIV_));
            System.out.println("Server IV: " + Arrays.toString(this.serverIV_));
        }
    }
}