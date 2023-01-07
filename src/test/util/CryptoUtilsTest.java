package util;

import main.exceptions.CryptoException;
import org.junit.jupiter.api.BeforeEach;
import main.util.CryptoUtils;
import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilsTest {
    private static final char[] PASSWORD = "pwd1234".toCharArray();
    private static final char[] INCORRECT_PASSWORD = "IncorrectPwd1234".toCharArray();
    private static final String SECRET_DATA = "I Love Programming";
    private static final int SALT_LEN = 16;
    private CryptoUtils cryptoUtils;

    @BeforeEach
    public void runBefore(){
        try{
            cryptoUtils = new CryptoUtils(PASSWORD);
        } catch (CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testGenerateAESKeyFromSamePasswordAndSalt() {
        SecretKey key1 = null;
        SecretKey key2 = null;
        try {
            byte[] salt = cryptoUtils.generateSecureBytes(SALT_LEN);
            key1 = cryptoUtils.generateAESKey(PASSWORD, salt);
            key2 = cryptoUtils.generateAESKey(PASSWORD, salt);
        } catch (CryptoException e) {
            fail();
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
        assertEquals(new String(key1.getEncoded()), new String(key2.getEncoded()));
    }

    @Test
    public void testGenerateAESKeyFromDiffPassword(){
        SecretKey key1 = null;
        SecretKey key2 = null;
        try {
            byte[] salt = cryptoUtils.generateSecureBytes(SALT_LEN);
            key1 = cryptoUtils.generateAESKey(PASSWORD, salt);
            key2 = cryptoUtils.generateAESKey("IncorrectPassword".toCharArray(), salt);
        } catch (CryptoException e) {
            fail();
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
        assertNotEquals(new String(key1.getEncoded()), new String(key2.getEncoded()));
    }

    @Test
    public void testGenerateAESKeyFromDiffSalt(){
        SecretKey key1 = null;
        SecretKey key2 = null;
        try {
            byte[] salt1 = cryptoUtils.generateSecureBytes(SALT_LEN);
            key1 = cryptoUtils.generateAESKey(PASSWORD, salt1);
            byte[] salt2 = cryptoUtils.generateSecureBytes(SALT_LEN);
            key2 = cryptoUtils.generateAESKey(PASSWORD, salt2);
        } catch (CryptoException e) {
            fail();
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
        assertNotEquals(new String(key1.getEncoded()), new String(key2.getEncoded()));
    }

    @Test
    public void testEncryptDecryptCorrectPassword(){
        byte[] decrypted = new byte[0];
        try {
            cryptoUtils = new CryptoUtils(PASSWORD);
            byte[] encrypted = encryptTestData();
            decrypted = cryptoUtils.decrypt(encrypted, PASSWORD);
        } catch (CryptoException e) {
            fail(e);
        }
        assertEquals(SECRET_DATA, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    public void testEncryptDecryptIncorrectPassword() {
        byte[] decrypted = new byte[0];
        byte[] encrypted = encryptTestData();
        try {
            decrypted = cryptoUtils.decrypt(encrypted, INCORRECT_PASSWORD);
            fail("Decrypted with incorrect password");
        } catch (CryptoException e) {
            // caught succesfully
        }
        assertNotEquals(SECRET_DATA, decrypted);
    }

    public byte[] encryptTestData(){
        try{
            return cryptoUtils.encrypt(SECRET_DATA.getBytes());
        } catch (CryptoException e) {
            fail("Ecryption Failed",e);
        }
        return null;
    }

}
