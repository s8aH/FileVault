package main.ui;

import javax.swing.*;

public class App {
    public static final int WIN_WIDTH = 800;
    public static final int WIN_HEIGHT = 600;
    private static JFrame frame;
    private static Screen screen;

    // getters
    public static Screen getScreen(){return screen;}
    public static JFrame getFrame(){return frame;}

    public void start(){
        frame = new JFrame("File Vault");

        screen = new Screen();
        pushScreen(screen);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// the program stops
        frame.setVisible(true);
        frame.setSize(WIN_WIDTH, WIN_HEIGHT);
        frame.setResizable(true);
    }

    public void pushScreen(Screen screen){
        frame.setContentPane(screen);
        frame.validate();
    }

}
