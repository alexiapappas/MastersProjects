package TLS;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class MessageCrypto {
    private final Cipher encryptCipher_;
    private final Cipher decryptCipher_;
    private final Mac mac_;

    public MessageCrypto(SecretKey encKey, SecretKey macKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        encryptCipher_ = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptCipher_.init(Cipher.ENCRYPT_MODE, encKey, new IvParameterSpec(iv));

        decryptCipher_ = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher_.init(Cipher.DECRYPT_MODE, encKey, new IvParameterSpec(iv));

        mac_ = Mac.getInstance("HmacSHA256");
        mac_.init(macKey);
    }

    public byte[] encrypt(Serializable object) throws IOException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        byte[] data = baos.toByteArray();

        // Calculate MAC
        byte[] macBytes = mac_.doFinal(data);

        // Combine data and MAC
        byte[] payload = concat(data, macBytes);

        // Encrypt the payload
        byte[] encrypted = encryptCipher_.doFinal(payload);

        return encrypted;
    }

    public Object decrypt(byte[] encrypted) throws IOException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
        byte[] decrypted = decryptCipher_.doFinal(encrypted);

        // Split data and MAC (last 32 bytes)
        byte[] data = Arrays.copyOf(decrypted, decrypted.length - 32);
        byte[] receivedMac = Arrays.copyOfRange(decrypted, decrypted.length - 32, decrypted.length);

        // Verify MAC
        byte[] computedMac = mac_.doFinal(data);
        if (!MessageDigest.isEqual(computedMac, receivedMac)) {
            throw new SecurityException("MAC verification failed");
        }

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object result = ois.readObject();
        ois.close();

        return result;
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}