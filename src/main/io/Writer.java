package main.io;

import com.google.gson.JsonObject;
import main.util.JsonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Writer {

    private File outFile;

    public Writer(File outputFile){
        this.outFile = outputFile;
    }

    // EFFECTS: writes given string to file
    public void writeString(String outputString) throws IOException {
        FileOutputStream out = new FileOutputStream(outFile);
        out.write(outputString.getBytes());
        out.close();
    }

    // EFFECTS: writes given byte array to file
    public void writeBytes(byte[] outputByteArray) throws IOException {
        FileOutputStream out = new FileOutputStream(outFile);
        out.write(outputByteArray);
        out.close();
    }

    public void writeJson(JsonObject obj) throws IOException {
        writeString(JsonUtils.toJsonString(obj));
    }
}
