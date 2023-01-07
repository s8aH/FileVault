package main.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
    private File file;

    public Reader(File inputFile) {
        this.file = inputFile;
    }

    // EFFECTS: reads JSON from file
    public JsonObject readJson() throws IOException {
        JsonElement jsonElement = JsonParser.parseReader(new FileReader(file));
        JsonObject jo = jsonElement.getAsJsonObject();
        return jo;
//        return JsonUtils.parse(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
    }

    // EFFECTS: reads large file into byte array one byte at a time
    public byte[] readBytes() throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);
        int numBytes = in.read(bytes);
        in.close();
        return bytes;
    }


}
