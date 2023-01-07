package main.ui;

import javax.swing.*;
import java.awt.*;

public class Screen extends JPanel{
    protected main.ui.MenuBar menuBar;
    protected VaultFinder finder;
    protected StatusBar statusBar;

    public Screen(){
        onOpen();
    }

    // main application screen
    public void onOpen(){
        this.setLayout(new BorderLayout());

        menuBar = new MenuBar();
        menuBar.makeMenu();

        finder = new VaultFinder();
        statusBar = new StatusBar();

        this.add(menuBar, BorderLayout.NORTH);
        this.add(finder, BorderLayout.CENTER);
        this.add(statusBar, BorderLayout.SOUTH);
    }




}
