package main.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

public class MenuBar extends JMenuBar {
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenuItem closeVault;
    private JPanel dialogPane;
    private JTextField nameField, dirField;
    private JPasswordField pwdField;
    private JButton dirBtn;


    // creates menus and add all menus to menu bar
    public void makeMenu() {
        makeFileMenu();
        makeViewMenu();
        makeEditMenu();
        enableLoadedVaultMenu(false);
    }

    // EFFECTS: creates File menu
    private void makeFileMenu() {
        fileMenu = new JMenu("File");
        this.add(fileMenu);

        JMenuItem newVault = new JMenuItem("New Vault");
        JMenuItem openVault = new JMenuItem("Open Vault");
        closeVault = new JMenuItem("Close Vault");
        JMenuItem exit = new JMenuItem("Exit");

        // add action listeners
        newVault.addActionListener(e -> handleNewVault());
        openVault.addActionListener(e -> handleOpenVault());
        closeVault.addActionListener(e -> App.getScreen().finder.closeVault());
        exit.addActionListener(e -> System.exit(0));

        // add menu items to File menu
        fileMenu.add(newVault);
        fileMenu.add(openVault);
        fileMenu.addSeparator();
        fileMenu.add(closeVault);
        fileMenu.addSeparator();
        fileMenu.add(exit);
    }


    // EFFECTS: creates Edit menu
    private void makeEditMenu() {
        editMenu = new JMenu("Edit");
        this.add(editMenu);

        // add menu items to Edit menu
        JMenuItem newFolder = new JMenuItem("New Folder");
        JMenuItem addFile = new JMenuItem("Add File");
        JMenuItem saveFile = new JMenuItem("Save File");
        JMenuItem delete = new JMenuItem("Delete");

        addFile.addActionListener(e -> handleAddFile());
        newFolder.addActionListener(e -> handleNewFolder());
        saveFile.addActionListener(e -> handleSaveFile());
        delete.addActionListener(e -> App.getScreen().finder.delete());

        editMenu.add(newFolder);
        editMenu.add(addFile);
        editMenu.add(saveFile);
        editMenu.add(delete);
    }

    // EFFECTS: creates View menu
    private void makeViewMenu() {
        viewMenu = new JMenu("View");
        this.add(viewMenu);
        // add menu items to View menu
        JCheckBoxMenuItem showStatusBar = new JCheckBoxMenuItem("Status Bar");
        showStatusBar.setSelected(true);
        showStatusBar.addActionListener(e -> App.getScreen().statusBar.setVisible(showStatusBar.isSelected()));
        viewMenu.add(showStatusBar);
    }

    // EFFECTS: prompts user to set vault name, password, and destination directory
    private void handleNewVault() {
        JFileChooser chooser = new JFileChooser();
        dialogPane = new JPanel();
        dialogPane.setLayout(new BoxLayout(dialogPane, BoxLayout.Y_AXIS));
        JPanel fieldPane = new JPanel();
        fieldPane.setLayout(new GridLayout(5, 2));

        JTextArea statusBar = new JTextArea();
        statusBar.setEditable(false);
        statusBar.setBackground(Color.lightGray);
        statusBar.setForeground(Color.RED);

        dialogPane.add(fieldPane);
        dialogPane.add(statusBar);

        nameField = new JTextField();
        pwdField = new JPasswordField();
        JCheckBox showPwdButton = new JCheckBox("show");
        showPwdButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    pwdField.setEchoChar((char) 0);
                else
                    pwdField.setEchoChar('\u25CF');
            }
        });
        dirField = new JTextField();
        dirBtn = new JButton("Browse...");
        dirField.setEditable(false);
        dirBtn.addActionListener(e -> handleChooseDir(chooser));

        fieldPane.add(new JLabel("Name of New Vault: "));
        fieldPane.add(nameField);
        fieldPane.add(new JLabel("Password for New Vault: "));
        fieldPane.add(pwdField);
        fieldPane.add(new JLabel()); // empty block
        fieldPane.add(showPwdButton);
        fieldPane.add(new JLabel("Select Destination Folder: "));
        fieldPane.add(dirField);
        fieldPane.add(new JLabel()); // empty block
        fieldPane.add(dirBtn);

        String ok = "OK";
        String cancel = "cancel";
        Object[] options = {ok, cancel};
        JOptionPane optionPane = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options,
                options[0]);
        optionPane.add(dialogPane, 0);
        JDialog dialog = new JDialog(App.getFrame(), "Please Fill Out All Fields", true);
        dialog.setContentPane(optionPane);

        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (isVisible()
                        && (e.getSource() == optionPane)
                        && (JOptionPane.VALUE_PROPERTY.equals(prop)
                        || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
                    Object value = optionPane.getValue(); // get the selected value

                    if (value == JOptionPane.UNINITIALIZED_VALUE) {
                        //ignore reset
                        return;
                    }

                    //Reset the JOptionPane's value.
                    //If you don't do this, then if the user
                    //presses the same button next time, no
                    //property change event will be fired.
                    optionPane.setValue(
                            JOptionPane.UNINITIALIZED_VALUE);

                    String message = "";
                    if (ok.equals(value)) {
                        if (nameField.getText().matches("[\\/:*?\"<>|]+")) {
                            message = "•A filename cannot contain any of the following \n characters: \\ / : * ? \" < > |"; // the characters have special meaning to Windows and could interfere with parsing a command line (or path).
                        }
                        if (nameField.getText().isBlank() || pwdField.getPassword().length == 0) {
                            String temp = "•Name and password cannot be empty";
                            message = message == "" ? temp : message + "\n" + temp;
                        } else {
                            dialog.dispose();
                            App.getScreen().statusBar.showStatus("Created Vault \"" + nameField.getName() +
                                    "\"", Color.BLACK);
                            App.getScreen().finder.loadVault(nameField.getText(), chooser.getSelectedFile(), pwdField.getPassword());
                        }
                        statusBar.setText(message);
                        statusBar.setForeground(Color.RED);
                        dialog.pack();
                    } else { //user closed dialog or clicked cancel
                        dialog.dispose(); // clears the dialog and hides it
                    }
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);
    }

    private void handleSaveFile() {
        App.getScreen().finder.saveFile();
    }

    // EFFECTS: prompts user to choose destination directory to create vault in
    private void handleChooseDir(JFileChooser chooser) {
        // JFileChooser chooser = new JFileChooser();
        File workingDirectory = new File(System.getProperty("user.dir"));
        chooser.setCurrentDirectory(workingDirectory);
        //chooser.setCurrentDirectory(new java.io.File(".")); // dialog starts at current directory, where
        // the program is running
        chooser.setDialogTitle("Select Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false); // disable the "ALL files" option

        if (chooser.showDialog(this, "Done") == JFileChooser.APPROVE_OPTION) {
            File destination = chooser.getSelectedFile();
            if (new File(destination, nameField.getText()).exists()) {
                JOptionPane.showMessageDialog(this, "A directory with the name " +
                        destination.getName() + "already exists in" + destination.getAbsolutePath(), "File Exists", JOptionPane.ERROR_MESSAGE);
                dirField.setText("");
            } else {
                dirField.setText(destination.getAbsolutePath());
            }
        }
    }

    // prompts user for vault directory and password
    private void handleOpenVault() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(".")); // dialog starts at current directory, where
        // the program is running
        chooser.setDialogTitle("Select Vault"); //
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false); // disable the "ALL files" option

        int returnValue = chooser.showDialog(this, "Done");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File destination = chooser.getSelectedFile();
            if (destination == null) {
                App.getScreen().statusBar.showStatus("No Vault Selected. No Vault Loaded", Color.red);
                return;
            } else if (!new File(destination, destination.getName() + ".vault").exists()) { // if selected directory doesn't contain .vault file then it is not a vault
                JOptionPane.showMessageDialog(this,
                        "The Directory \"" + destination.getAbsolutePath() + "\" Does Not Have a \"" +
                                destination.getName() + ".vault\" File Associated With It. Please Select a Vault Directory.",
                        "The Directory Does Not Appear To Be a Vault!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(this, pf, "Enter Password to " +
                    destination.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            String password = String.valueOf(pf.getPassword());

            if (okCxl == JOptionPane.OK_OPTION) {
                if (!password.isBlank())
                    App.getScreen().finder.loadVault(null, destination, password.toCharArray());
            }
        }
    }

    // EFFECTS: prompts user to select file to add
    private void handleAddFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select File to Add");
        int status = chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();
        if (status == JFileChooser.APPROVE_OPTION) {
            if (file == null) return;
            App.getScreen().finder.addFile(file);
        }
    }

    //EFFECTS: prompts user for new folder name and create new folder
    private void handleNewFolder() {
        String name = JOptionPane.showInputDialog("Folder Name: ");
        if (name.isBlank()) return;
        App.getScreen().finder.createFolder(name);
    }

    //EFFECTS: enables or disables menu items relevant to loaded vault
    protected void enableLoadedVaultMenu(boolean state) {
        Arrays.stream(editMenu.getMenuComponents()).forEach(x -> x.setEnabled(state));
        closeVault.setEnabled(state);
    }

}
