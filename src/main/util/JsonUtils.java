package main.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class JsonUtils {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // GETTERS
    public static Gson getGson(){return gson;}

    // EFFECTS: converts JsonObject to JSON-formatted string
    public static String toJsonString(JsonObject obj) {
        return gson.toJson(obj);
    }

    // EFFECTS: converts JSON-formatted string to JsonObject
    public static JsonObject parse(String json){
        return gson.fromJson(json, JsonObject.class);
    }
}
