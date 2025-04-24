package TLS;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.*;

public class HandshakeUtil {
    // Consistent HMAC generation for both client and server
    public static byte[] generateFinishedMessage(byte[] clientNonce,
                                                 X509Certificate serverCert,
                                                 BigInteger serverDHPub,
                                                 byte[] signedServerDHPub,
                                                 X509Certificate clientCert,
                                                 BigInteger clientDHPub,
                                                 byte[] signedClientDHPub,
                                                 SecretKey macKey,
                                                 boolean isServer) throws NoSuchAlgorithmException, InvalidKeyException, CertificateEncodingException {

        // Create a digest of all handshake messages in the exact same order
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // 1. Client Hello (nonce)
        digest.update(clientNonce);

        // 2. Server Hello (cert, DH pub, signed DH pub)
        digest.update(serverCert.getEncoded());
        digest.update(serverDHPub.toByteArray());
        digest.update(signedServerDHPub);

        // 3. Client Response (cert, DH pub, signed DH pub)
        digest.update(clientCert.getEncoded());
        digest.update(clientDHPub.toByteArray());
        digest.update(signedClientDHPub);

        // If we're verifying the server's message, we stop here
        // If we're verifying the client's message, we include the server's finished message

        byte[] transcriptHash = digest.digest();

        // Now compute the HMAC of the transcript
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(macKey);
        byte[] result = hmac.doFinal(transcriptHash);

        return result;
    }

    // Generate client finished message
    public static byte[] generateClientFinished(byte[] clientNonce,
                                                X509Certificate serverCert,
                                                BigInteger serverDHPub,
                                                byte[] signedServerDHPub,
                                                X509Certificate clientCert,
                                                BigInteger clientDHPub,
                                                byte[] signedClientDHPub,
                                                byte[] serverFinishedHmac,
                                                SecretKey clientMacKey) throws NoSuchAlgorithmException, InvalidKeyException, CertificateEncodingException {

        // For client finished, we need to include server finished in the transcript
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // 1. Client Hello (nonce)
        digest.update(clientNonce);

        // 2. Server Hello (cert, DH pub, signed DH pub)
        digest.update(serverCert.getEncoded());
        digest.update(serverDHPub.toByteArray());
        digest.update(signedServerDHPub);

        // 3. Client Response (cert, DH pub, signed DH pub)
        digest.update(clientCert.getEncoded());
        digest.update(clientDHPub.toByteArray());
        digest.update(signedClientDHPub);

        // 4. Server finished message
        digest.update(serverFinishedHmac);

        byte[] transcriptHash = digest.digest();

        // Now compute the HMAC of the transcript
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(clientMacKey);
        byte[] result = hmac.doFinal(transcriptHash);

        return result;
    }

    // Helper method to print debug info about messages
    public static void printMessageInfo(String title, byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            System.out.println(title + " (" + data.length + " bytes): " +
                    Arrays.toString(digest.digest(data)));
        } catch (Exception e) {
            System.err.println("Error hashing " + title + ": " + e.getMessage());
        }
    }
}