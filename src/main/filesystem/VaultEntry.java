package main.filesystem;

import com.google.gson.JsonObject;
import main.io.Jsonable;

import java.util.ArrayList;
import java.util.Stack;
import java.util.UUID;

/**
 * represents an entry that can either be a file entry or a directory entry
 */
public abstract class VaultEntry implements Jsonable {
    protected long size;
    private UUID id;
    private String name;
    public VaultEntry(String id, String name){
        this.id = UUID.fromString(id);
        this.name = name;
    }

    // GETTERS
    public String getId(){return id.toString();}
    public String getName(){return name;}

    public long getSize(){return size;}

    public abstract JsonObject toJson();
}
