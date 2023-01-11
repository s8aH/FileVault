package main.ui;

import main.filesystem.Vault;
import main.filesystem.VaultDirectory;
import main.filesystem.VaultEntry;
import main.filesystem.VaultFile;
import main.exceptions.CryptoException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class VaultFinder extends JPanel {

    private JTextField address;
    private JButton back, reload;
    private Vault vault;
    private VaultDirectory currDir;
    private DefaultListModel model;
    private JList list;
    final String[] colHeads = {"Icon", "File Name", "Size (in Bytes)", "ID"};
    private JLabel fileName, size, id;

    // EFFECTS: adds all components to panel
    public VaultFinder() {
        setBorderLayout();
        addAddressBar();
        addFinder();
        addDetailView();
    }

    private void setBorderLayout() {
        this.setLayout(new BorderLayout());
    }

    // EFFECTS: make address bar and add it to panel
    private void addAddressBar() {
        JPanel adrPane = new JPanel();
        adrPane.setLayout(new BoxLayout(adrPane, BoxLayout.X_AXIS));

        address = new JTextField();
        address.setEditable(false);
        address.setColumns(35);

        back = new JButton("<");
        back.setEnabled(false);
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshList(vault.getRoot());
            }
        });

        reload = new JButton("↻");
        reload.addActionListener(e -> refreshList(currDir));
        reload.setFont(new Font("Arial", Font.PLAIN, 15));

        adrPane.add(back);
        adrPane.add(address);
        adrPane.add(reload);

        this.add(adrPane, BorderLayout.NORTH);
    }

    // EFFECTS: adds finder to panel
    private void addFinder() {
        model = new DefaultListModel<>();
        list = new JList(model);
        createFinder();
    }

    // EFFECTS: adds Finder to pane
    private void createFinder() {
        JScrollPane scrollPane = new JScrollPane(list);
        list.addMouseListener(new MouseAdapter() {
                                  @Override
                                  public void mouseClicked(MouseEvent e) {
                                      String selected = (String) list.getSelectedValue();
                                      if (selected == null) return;
                                      for (VaultEntry vaultEntry : currDir.getEntries()) {
                                          if (selected.equals(vaultEntry.getName())) {
                                              // double-clicked
                                              if (e.getClickCount() % 2 == 0) {
                                                  if (vaultEntry.getClass().equals(VaultDirectory.class)) {
                                                      refreshList((VaultDirectory) vaultEntry);
                                                  } else {
                                                      openImageFile((VaultFile) vaultEntry);
                                                  }
                                                  // single-clicked
                                              } else if (e.getClickCount() == 1)
                                                  setFileDetails(vaultEntry);
                                              break;
                                          }
                                      }
                                  }
                              }

        );
        this.add(scrollPane, BorderLayout.CENTER);
    }

    // EFFECTS: adds detail view to pane
    private void addDetailView() {
        // shows details for a file
        JPanel fileMainDetails = new JPanel(new BorderLayout(4, 4));
        fileMainDetails.setBorder(BorderFactory.createLineBorder(Color.black));

        JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
        fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

        JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
        fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

        fileDetailsLabels.add(new JLabel("File Name", JLabel.TRAILING));
        fileName = new JLabel();
        fileDetailsValues.add(fileName);

        fileDetailsLabels.add(new JLabel("ID", JLabel.TRAILING));
        id = new JLabel();
        fileDetailsValues.add(id);

        fileDetailsLabels.add(new JLabel("File size (in Bytes)", JLabel.TRAILING));
        size = new JLabel();
        fileDetailsValues.add(size);

        int count = fileDetailsLabels.getComponentCount();
        for (int ii = 0; ii < count; ii++) {
            fileDetailsLabels.getComponent(ii).setEnabled(false);
        }


        this.add(fileMainDetails, BorderLayout.SOUTH);
    }

    private void setFileDetails(VaultEntry selected) {
        if(selected.getClass().equals(VaultDirectory.class))
            fileName.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        else
            fileName.setIcon(UIManager.getIcon("FileView.fileIcon"));
        fileName.setText(selected.getName());
        size.setText(selected.getSize() + " bytes");
        id.setText(selected.getId());

    }

    // EFFECTS: updates current directory with dir and updates list of files
    private void refreshList(VaultDirectory dir) {
        currDir = dir;
        address.setText("/" + vault.getRoot().getPathOfEntry(currDir.getId(), true));
        model.clear();
        model.addAll(dir.getEntries().stream().map(VaultEntry::getName).collect(Collectors.toList()));
    }


    // EFFECTS: opens image file in pop-up window
    private void openImageFile(VaultFile file) {
        String extension = file.getName().substring(file.getName().lastIndexOf('.'));
        if (extension.toLowerCase().matches(".(png|jpg|jpeg|gif|svg)")) {
            App.getScreen().statusBar.showStatus("Opening " + file.getName(), Color.WHITE);
            try {
                JDialog dialog = new JDialog();
                dialog.setTitle(file.getName());
                dialog.setUndecorated(false);
                BufferedImage bImage = ImageIO.read(new ByteArrayInputStream(vault.open(file)));
                JLabel label = new JLabel(new ImageIcon(bImage));
                dialog.add(label);
                dialog.pack();
                dialog.setVisible(true);
            } catch (IOException e) {
                App.getScreen().statusBar.showError("opening file", StatusBar.Error.IO, e);
            } catch (CryptoException e) {
                App.getScreen().statusBar.showError("opening file", StatusBar.Error.CRYPTO, e);
            }
        } else {
            App.getScreen().statusBar.showError("opening unsupported file", StatusBar.Error.DEFAULT,
                    new Exception(file.getName()));
        }
    }

    // EFFECTS: if name is null, load existing vault at directory;
    // otherwise, create new vault in directory
    public void loadVault(String name, File directory, char[] password) {
        try {
            if (name == null) {
                vault = new Vault(directory, password); // load vault
            } else {
                vault = new Vault(new File(directory, name), password); // create new vault
            }
        } catch (IOException e) {
            App.getScreen().statusBar.showError("Loading Vault", StatusBar.Error.IO, e);
            return;
        } catch (CryptoException e) {
            App.getScreen().statusBar.showError("Incorrect Password for \"" + name + "\". Re-Open the Vault With Correct Password", StatusBar.Error.CRYPTO, e);
            return;
        }
        refreshList(vault.getRoot());
        enableLoadedVaultUI(true);
        App.getScreen().menuBar.enableLoadedVaultMenu(true);
        App.getFrame().setTitle(vault.getVaultFolder().getName());
        App.getScreen().statusBar.showStatus("Loaded Vault \"" + vault.getVaultFolder().getName() + "\"", Color.WHITE);
    }

    private void enableLoadedVaultUI(boolean state) {
        back.setEnabled(state);
        address.setEnabled(state);
    }

    // EFFECTS: adds file to vault and reloads list
    protected void addFile(File file) {
        try {
            vault.addFile(file, currDir);
            refreshList(currDir);
            App.getScreen().statusBar.showStatus("Added File \"" + file.getName() + "\" Under \"/"
                    + vault.getRoot().getPathOfEntry(currDir.getId(), true) + "\"", Color.WHITE);
        } catch (IOException e) {
            App.getScreen().statusBar.showError("Adding File", StatusBar.Error.IO, e);
        } catch (CryptoException e) {
            App.getScreen().statusBar.showError("Adding File", StatusBar.Error.CRYPTO, e);
        }
    }

    // EFFECTS: creates VaultDirectory under currDir with name and reloads list
    protected void createFolder(String name) {
        try {
            vault.createFolder(name, currDir);
            refreshList(currDir);
            App.getScreen().statusBar.showStatus("Successfully Created New Folder \"" + name + "\" Under \"/" +
                    vault.getRoot().getPathOfEntry(currDir.getId(), true) + "\"", Color.BLUE);
        } catch (IOException e) {
            App.getScreen().statusBar.showError("Creating Folder", StatusBar.Error.IO, e);
        }
    }

    // EFFECTS: decrypts and saves selected file locally under selected destination directory
    protected void saveFile() {
        // find the first vault entry that matches with the name of the selected file
        Optional<VaultEntry> vaultFile = currDir.getEntries().stream().filter(
                x -> x.getName().equals(list.getSelectedValue().toString())).findFirst();
        if (vaultFile.isPresent() && vaultFile.get().getClass().equals(VaultFile.class)) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Destination Folder"); //
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int destination = chooser.showSaveDialog(null);
            if (destination == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                App.getScreen().statusBar.setText("Saving: " + vaultFile.get().getName() + ".");
                try {
                    vault.saveFile(vaultFile.get().getName(), selected);
                    App.getScreen().statusBar.showStatus("Saved \"" + vaultFile.get().getName() + "\" Under \"" +
                            selected.getAbsolutePath() + "\"", Color.WHITE);
                } catch (IOException e) {
                    App.getScreen().statusBar.showError("Saving File", StatusBar.Error.IO, e);
                } catch (CryptoException e) {
                    App.getScreen().statusBar.showError("Saving File", StatusBar.Error.CRYPTO, e);
                }
            } else {
                App.getScreen().statusBar.setText("Save Command Cancelled By User.");
            }
        }
    }

    protected void closeVault() {
        try {
            vault.lock();
            enableLoadedVaultUI(false);
            App.getScreen().menuBar.enableLoadedVaultMenu(false);
            App.getFrame().setTitle(null);
            App.getScreen().statusBar.showStatus("\"" + vault.getVaultFolder().getName() + "\" Closed", Color.WHITE);
            model.clear();
        } catch (IOException e) {
            App.getScreen().statusBar.showError("Saving Vault", StatusBar.Error.IO, e);
        }
    }

    // EFFECTS: deletes selected entry from vault and reloads list
    public void delete() {
        Optional<VaultEntry> vaultFile = currDir.getEntries().stream().filter(
                x -> x.getName().equals(list.getSelectedValue().toString())).findFirst();
        if (vaultFile != null) {
            try {
                vault.delete(vaultFile.get(), currDir);
                refreshList(currDir);
                App.getScreen().statusBar.showStatus("Deleted Entry \"" + vaultFile.get().getName() + "\" under \"/"
                        + vault.getRoot().getPathOfEntry(currDir.getId(), true) + "\"", Color.WHITE);
            } catch (IOException e) {
                App.getScreen().statusBar.showError("Deleting Entry", StatusBar.Error.IO, e);
            }
        } else {
            App.getScreen().statusBar.showStatus("No Entry Selected", Color.red);
        }
    }

}
