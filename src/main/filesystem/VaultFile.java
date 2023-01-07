package main.filesystem;

import com.google.gson.JsonObject;
import java.util.ArrayList;

public class VaultFile extends VaultEntry{

    public VaultFile(String id, String name, long size) {
        super(id, name);
        this.size = size;
    }

//    @Override
//    public String getPathOfEntry(String id, Stack<VaultEntry> stack) {
//        return null;
//    }

    // EFFECTS: returns JsonObject containing data of this entry
    @Override
    public JsonObject toJson() {
        JsonObject fileJson = new JsonObject();
        fileJson.addProperty("id",getId());
        fileJson.addProperty("name",getName());
        fileJson.addProperty("size",getSize());
        return fileJson;
    }
}
