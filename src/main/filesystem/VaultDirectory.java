package main.filesystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.*;

/**
 * represents a directory entry
 */
public class VaultDirectory extends VaultEntry {
    private ArrayList<VaultEntry> entries;

    // EFFECTS: constructs new directory with given id, name, zero size, empty entries
    public VaultDirectory(String id, String name) {
        super(id, name);
        this.size = 0;
        entries = new ArrayList<>();
    }

    // GETTERS
    public ArrayList<VaultEntry> getEntries() {
        return entries;
    }

    // EFFECTS: recursively finds path of entry relative to current directory
    public String getPathOfEntry(String id, boolean useName){
        if(this.getId().equals(id)) return "";

        for(VaultEntry entry: entries){
            String path = useName ? entry.getName() : entry.getId();
            if(entry.getId().equals(id)){
                return path;
            }else if(entry.getClass().equals(VaultDirectory.class)){
                String res = ((VaultDirectory) entry).getPathOfEntry(id, useName);
                if(res != null)
                    return path + "/" + res;
            }
        }
        return null;
    }

    // EFFECTS: adds an entry to the directory
    public void addEntry(VaultEntry entry) {
        entries.add(entry);
        size += entry.getSize();
    }

    // EFFECTS: recursively adds all entries from JSON Array
    public void addEntries(JsonArray entries) {
        for (JsonElement e : entries) {
            JsonObject obj = (JsonObject) e;
            if (obj.has("entries")) {
                VaultDirectory dir = new VaultDirectory(obj.get("id").getAsString(), obj.get("name").getAsString());
                dir.addEntries(obj.get("entries").getAsJsonArray());
                addEntry(dir);
            } else {
                VaultFile file = new VaultFile(obj.get("id").getAsString(), obj.get("name").getAsString(),
                        obj.get("size").getAsLong());
                addEntry(file);
            }
        }
    }

    // EFFECTS: deletes an entry from the directory
    public void deleteEntry(VaultEntry entry) {
        entries.remove(entry);
        size -= entry.getSize();
    }

    // EFFECTS: returns JsonObject containing data of this directory and its sub-entries
    @Override
    public JsonObject toJson() {
        JsonObject dirJson = new JsonObject();
        dirJson.addProperty("id", getId());
        dirJson.addProperty("name", getName());
        dirJson.addProperty("size", size);

        JsonArray entriesJson = new JsonArray();
        dirJson.add("entries", entriesJson);
        for (VaultEntry entry : entries) {
            entriesJson.add(entry.toJson());
        }

        return dirJson;
    }

    // EFFECTS: recursively finds entry with given id
    public VaultEntry getEntryById(String id, ArrayList<VaultEntry> entries){
        for(VaultEntry e:entries){
            if(e.getClass().equals(VaultDirectory.class)){
                if(e.getId().equals(id)) {
                    return e;
                }
                VaultEntry res = getEntryById(id, ((VaultDirectory) e).entries);
                if(res!=null)
                    return res;
            }
            else{
                if(e.getId().equals(id))
                    return e;
            }
        }
        return null;
    }

}
