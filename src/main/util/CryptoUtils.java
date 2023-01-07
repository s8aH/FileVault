package main.util;

import com.google.gson.JsonObject;
import main.exceptions.CryptoException;
import main.io.Jsonable;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoUtils implements Jsonable {
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    protected static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    protected static final int ITERATION_COUNT = 65536;
    protected static final int KEY_SIZE = 256;
    private int T_LEN = 128;

    private char[] password;
    private Cipher aes;

    // EFFECTS: creates instance from Cipher class
    public CryptoUtils(char[] password) throws CryptoException {
        this.password = password;
        initCipher();
    }

    // EFFECTS: initiates AES cipher
    private void initCipher() throws CryptoException {
        try {
            aes = Cipher.getInstance(ENCRYPT_ALGO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoException(e);
        }
    }

    // EFFECTS: generates a secret key from salt and password
    public SecretKey generateAESKey(char[] password, byte[] salt) throws CryptoException {
        // generates key
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_SIZE); // instantiate password-based encryption specification
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM); // used to generate key
            SecretKey key = factory.generateSecret(spec);
            SecretKey keySpec = new SecretKeySpec(key.getEncoded(), "AES"); // generate secret key specs
            return keySpec;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    // EFFECTS: securely generates random bytes of the specified length
    public static byte[] generateSecureBytes(int length) throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstanceStrong();
        byte[] bytes = new byte[length];
        sr.nextBytes(bytes);
        return bytes;
    }

    // EFFECTS: encrypts input data using key
    public byte[] encrypt(byte[] input) throws CryptoException {
        try {
            // generate salt
            byte[] salt = generateSecureBytes(SALT_LENGTH);
            // generate key from password
            SecretKey key = generateAESKey(password, salt);
            // generate IV
            byte[] iv = generateSecureBytes(IV_LENGTH);
            GCMParameterSpec ivParams = new GCMParameterSpec(T_LEN, iv);

            // initialize AES encryption
            aes.init(Cipher.ENCRYPT_MODE, key, ivParams); // initializes cipher to encrypt

            byte[] cipherText = aes.doFinal(input); // encrypts the input

            // prepend IV and Salt to cipher text
            byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
                    .put(iv)
                    .put(salt)
                    .put(cipherText)
                    .array();

            return cipherTextWithIvSalt;
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException | NoSuchAlgorithmException e){
            throw new CryptoException(e);
        }
    }

    //EFFECTS: decrypts input data
    public byte[] decrypt(byte[] cipherText, char[] password) throws CryptoException {
        try {
            // split into iv, salt, and cipher text
            ByteBuffer bb = ByteBuffer.wrap(cipherText);
            byte[] iv = new byte[IV_LENGTH];
            bb.get(iv);
            byte[] salt = new byte[SALT_LENGTH];
            bb.get(salt);
            byte[] encrypted = new byte[bb.remaining()];
            bb.get(encrypted);

            // get back key from password and salt
            SecretKey key = generateAESKey(password, salt);

            // decrypt
            GCMParameterSpec spec = new GCMParameterSpec(T_LEN, iv);
            aes.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plainText = aes.doFinal(encrypted);
            //return new String(plainText, UTF_8);
            return plainText;
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                 | BadPaddingException e){
            throw new CryptoException(e);
        }
    }

    // EFFECTS: Encodes the specified byte array into a String using the Base64 encoding scheme
    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    // EFFECTS: Decodes a Base64 encoded String into a newly-allocated UTF-8 byte array using the Base64 encoding scheme
    public static byte[] decode(String data) {
        return Base64.getDecoder().decode(data.getBytes(UTF_8));
    }

    // EFFECTS: sets password to null
    public void destroy(){
        password = null;
    }

    @Override
    public JsonObject toJson() {
        JsonObject crypto = new JsonObject();
        return crypto;
    }
}
