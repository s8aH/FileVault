package main.filesystem;


import com.google.gson.JsonObject;
import main.exceptions.CryptoException;
import main.io.Reader;
import main.io.Writer;
import main.util.CryptoUtils;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;

/**
 * handles filesystem entries and functionality of a vault
 */
public class Vault {
    protected static final String ROOT_ID = "00000000-0000-0000-0000-000000000000";
    protected static final String ROOT_NAME = "root";
    private File vaultFolder;
    private char[] pwd;
    private File dataFolder; // data folder
    private JsonObject contents; // contents of .vault JSON file
    public CryptoUtils cryptoUtils;
    protected VaultDirectory root; // VaultDirectory corresponding to data folder

    // EFFECTS: if already exists, loads existing vault filesystem; otherwise, creates new vault
    public Vault(File vaultFolder, char[] password) throws IOException, CryptoException {
        this.vaultFolder = vaultFolder;
        this.pwd = password;
        dataFolder = new File(vaultFolder, "data");
        File vault = new File(vaultFolder, vaultFolder.getName() + ".vault"); // json file
        root = new VaultDirectory(ROOT_ID, ROOT_NAME);
        if (vault.exists()) {
            loadVault(vault, password);
        } else {
            dataFolder.mkdirs();
            unlock(password);
            sync();
        }
    }

    // Getters
    public VaultDirectory getRoot() {
        return root;
    }

    public File getVaultFolder() {
        return vaultFolder;
    }

    public JsonObject getContents() {
        return contents;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    // EFFECTS: initializes CryptoUtils with password
    public void unlock(char[] password) throws CryptoException {
        cryptoUtils = new CryptoUtils(password);
    }

    // EFFECTS: destroys CryptoUtils and saves filesystem
    public void lock() throws IOException {
        sync();
        cryptoUtils.destroy();
    }

    // EFFECTS: saves filesystem data of this vault to filesystem.json in root folder
    public void sync() throws IOException {
        contents = new JsonObject();
        //contents.add("crypto", cryptoUtils.toJson());
        contents.add("filesystem", root.toJson());
        new Writer(new File(vaultFolder, vaultFolder.getName() + ".vault")).writeJson(contents);
    }

    // EFFECTS: adds encrypted contents of input file to vault directory
    public void addFile(File inputFile, VaultDirectory dir) throws IOException, CryptoException {
        // read as bytes and encrypt contents of input file
        byte[] inputFileInBytes = new Reader(inputFile).readBytes();
        byte[] encrypted = cryptoUtils.encrypt(inputFileInBytes);

        // write to file
        String id = UUID.randomUUID().toString();
        String pathFromRoot = dataFolder.getPath() + "/" + root.getPathOfEntry(dir.getId(), false);
        File outputFile = new File(pathFromRoot, id);
        new Writer(outputFile).writeBytes(encrypted);

        // add file to vault directory
        VaultFile file = new VaultFile(id, inputFile.getName(), inputFile.length());
        dir.addEntry(file);

        sync();
    }

    public void loadVault(File vault, char[] password) throws IOException, CryptoException {
        this.contents = new Reader(vault).readJson();
        unlock(password);
        root.addEntries(contents.getAsJsonObject("filesystem").getAsJsonArray("entries"));
    }

    // EFFECTS: creates new directory under given VaultDirectory
    public void createFolder(String name, VaultDirectory parent) throws IOException {
        String id = UUID.randomUUID().toString();
        String pathFromRoot = dataFolder.getPath() + "/" + root.getPathOfEntry(parent.getId(), false);
        boolean result = new File(pathFromRoot, id).mkdir();
        parent.addEntry(new VaultDirectory(id, name));
        sync();
    }

    // EFFECTS: decrypts and saves contents of fileName to outputDirectory
    public void saveFile(String fileName, File outputDirectory) throws IOException, CryptoException {
        for (VaultEntry entry : root.getEntries()) {
            if (entry.getName().equals(fileName)) {
                File encrypted = new File(dataFolder, root.getPathOfEntry(entry.getId(), false));
                byte[] decrypted = cryptoUtils.decrypt(new Reader(encrypted).readBytes(), pwd);
                new Writer(new File(outputDirectory, fileName)).writeBytes(decrypted);
                break;
            }
        }
    }

    // EFFECTS: decrypts and returns contents of file
    public byte[] open(VaultEntry entry) throws IOException, CryptoException {
        if (entry.getClass().equals(VaultFile.class)) {
            File encrypted = new File(dataFolder, root.getPathOfEntry(entry.getId(), false));
            return cryptoUtils.decrypt(new Reader(encrypted).readBytes(), pwd);
        } else {
            return new byte[0];
        }
    }

    // EFFECTS: deletes entry from directory and disk
    public void delete(VaultEntry entry, VaultDirectory directory) throws IOException {
        File file = new File(dataFolder, root.getPathOfEntry(entry.getId(), false)); // file corresponding to the given entry within data folder
        if (entry.getClass().equals(VaultFile.class)) {
            file.delete();
        } else {
            FileUtils.deleteDirectory(new File(dataFolder, root.getPathOfEntry(entry.getId(), false)));
        }
        directory.deleteEntry(entry);
        sync();
    }

}
