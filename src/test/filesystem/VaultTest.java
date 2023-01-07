package filesystem;

import com.google.gson.JsonObject;
import main.exceptions.CryptoException;
import main.filesystem.Vault;
import main.filesystem.VaultDirectory;
import main.io.Reader;
import main.util.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class VaultTest {
    private static Vault vault;
    private static final File VAULT_EXIST = new File("test_vault/vault_exists");
    private static final File VAULT_NOT_EXIST = new File("test_vault/vault_not_exist");

    private static final char[] TEST_PASSWORD = "pwd1234".toCharArray();

    @BeforeEach
    public void runBefore() {
        deleteTestVaults();
        try {
            vault = new Vault(VAULT_EXIST, TEST_PASSWORD);
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @AfterAll
    public static void deleteTestVaults() {
        try {
            FileUtils.deleteDirectory(VAULT_EXIST);
            FileUtils.deleteDirectory(VAULT_NOT_EXIST);
        } catch (IOException e) {

        }
    }

    @Test
    public void testConstructorExistingVault() {
        try {
            vault = new Vault(VAULT_EXIST, TEST_PASSWORD);
            assertTrue(vault.getVaultFolder().exists());
            assertTrue(vault.getDataFolder().exists());
            assertNotNull(vault.getContents());
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testConstructorCreateVault() {
        try {
            vault = new Vault(VAULT_NOT_EXIST, TEST_PASSWORD);
            assertTrue(vault.getVaultFolder().exists());
            assertTrue(vault.getDataFolder().exists());
            assertNotNull(vault.getContents());
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testAddFile() {
        try {
            vault = new Vault(VAULT_EXIST, TEST_PASSWORD);
            JsonObject original = vault.getContents(); // data folder is empty
            vault.addFile(new File("testReadWrite"), vault.getRoot()); // "testReadWrite" file should already be created in test_vault folder in advance
            //vault.sync();
            JsonObject updated = vault.getContents();

            assertEquals(1, vault.getDataFolder().listFiles().length);
            assertNotEquals(JsonUtils.getGson().toJson(original), JsonUtils.getGson().toJson(updated));
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testAddFileInFolder(){
        try {
            File file = new File("testReadWrite");
            vault.createFolder("testDir", vault.getRoot());
            vault.addFile(file, (VaultDirectory) vault.getRoot().getEntries().get(0));

            byte[] decrypted = vault.open(((VaultDirectory) vault.getRoot().getEntries().get(0)).getEntries().get(0));
            byte[] originalInBytes = new Reader(file).readBytes();

            assertEquals(((VaultDirectory) vault.getRoot().getEntries().get(0)).getEntries().size(), 1);
            assertEquals(vault.getDataFolder().listFiles()[0].listFiles().length, 1);
            assertEquals(new String(originalInBytes), new String(decrypted));
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteFile(){
        try {
            JsonObject original = vault.getContents(); // data folder is empty
            vault.addFile(new File("testReadWrite"), vault.getRoot());
            vault.delete(vault.getRoot().getEntries().get(0), vault.getRoot());
            vault.sync();
            JsonObject updated = vault.getContents();

            assertEquals(0, vault.getDataFolder().listFiles().length);
            assertEquals(JsonUtils.getGson().toJson(original), JsonUtils.getGson().toJson(updated));
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testOpenFile() {
        try {
            File original = new File("testReadWrite");
            vault.addFile(new File("testReadWrite"), vault.getRoot());
            byte[] decrypted = vault.open(vault.getRoot().getEntries().get(0));
            byte[] originalInBytes = new Reader(original).readBytes();
            assertEquals(new String(originalInBytes), new String(decrypted));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CryptoException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testOpenDirectory(){
        try {
            vault.createFolder("testDir", vault.getRoot());
            byte[] decrypted = vault.open(vault.getRoot().getEntries().get(0));
            assertEquals(new String(decrypted), new String(new byte[0]));
        } catch (IOException e) {
            fail();
        } catch (CryptoException e) {
            fail();
        }
    }

    @Test
    public void testOpenFileToBytes() {
        try {
            vault.addFile(new File("testReadWrite"), vault.getRoot());
            assertTrue(vault.open(vault.getRoot().getEntries().get(0)).length > 0);
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testOpenImage(){
        try{
            File img = new File("tiger.png");
            vault.addFile(img, vault.getRoot());
            byte[] results = vault.open(vault.getRoot().getEntries().get(0));
            assertEquals(new String(results), new String(new Reader(img).readBytes()));
        } catch (IOException | CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testUnlockNewSalt() {
        try {
            vault.unlock(TEST_PASSWORD);
            assertNotNull(vault.cryptoUtils);
        } catch (CryptoException e) {
            fail(e);
        }
    }

    @Test
    public void testCreateDirectory(){
        try{
            vault.createFolder("testDir",vault.getRoot());
        } catch (IOException e) {
            fail();
        }
        assertTrue(new File(vault.getDataFolder(),vault.getRoot().getEntries().get(0).getId()).exists());
        assertEquals(vault.getRoot().getEntries().size(),1);
        assertEquals(vault.getDataFolder().listFiles().length, 1);
    }

    @Test
    public void testCreateDirectoryInDirectory(){
        try{
            vault.createFolder("testDir",vault.getRoot());
            vault.createFolder("testDir2", (VaultDirectory) vault.getRoot().getEntries().get(0));
        } catch (IOException e) {
            fail();
        }
        assertTrue(new File(vault.getDataFolder(),
                vault.getRoot().getEntries().get(0).getId() + "/" +
                ((VaultDirectory) vault.getRoot().getEntries().get(0)).getEntries().get(0).getId()).exists());
        assertEquals(((VaultDirectory) vault.getRoot().getEntries().get(0)).getEntries().size(),1);
        assertEquals(vault.getDataFolder().listFiles()[0].listFiles().length, 1);
    }

    @Test
    public void testGetPathOfEntryUsingName(){
        String testDir2Path = "";
        try{
            vault.createFolder("testDir",vault.getRoot());
            vault.createFolder("testDir2", vault.getRoot());
            vault.addFile(new File("testReadWrite"), (VaultDirectory) vault.getRoot().getEntries().get(1));
            testDir2Path = vault.getRoot().getPathOfEntry(vault.getRoot().getEntries().get(1).getId(), true);
        } catch (IOException | CryptoException e) {
            fail();
        }
        assertEquals("testDir2", testDir2Path);
    }
}
