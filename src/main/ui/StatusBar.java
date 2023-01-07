package main.ui;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JTextArea {

    public enum Error{IO, CRYPTO, DEFAULT}

    public StatusBar(){
        super(2,1);
        this.setEditable(false);
        this.setLineWrap(true);
        this.setBackground(Color.BLACK);
        showStatus("No Vault Loaded", Color.RED);
    }

    // EFFECTS: displays text in given color
    public void showStatus(String status, Color color){
        this.setText(status);
        this.setFont(new Font("Courier New", Font.PLAIN, 12));
        this.setForeground(color);
    }

    // EFFECTS: displays error
    public void showError(String operation, Error err, Exception e){
        switch(err){
            case IO:
                this.setText(err + " error when " + operation + ": " + e.getMessage());
                break;
            case CRYPTO:
                this.setText(err + " error when " + operation + ". Please re-enter your password: " + e.getMessage());
                break;
            default:
                this.setText("Error when " + operation + ": " + e.getMessage());
        }
        this.setForeground(Color.RED);
    }
}
