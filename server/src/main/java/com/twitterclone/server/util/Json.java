package com.twitterclone.server.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Small helpers for safely reading fields out of a request payload and building
 * response payloads. Keeps the handlers free of repetitive null/parse checks.
 */
public final class Json {

    public static final Gson GSON = new Gson();

    private Json() {
    }

    public static String getString(JsonObject obj, String key) {
        if (obj == null) return null;
        JsonElement el = obj.get(key);
        return (el == null || el.isJsonNull()) ? null : el.getAsString();
    }

    public static Integer getIntOrNull(JsonObject obj, String key) {
        if (obj == null) return null;
        JsonElement el = obj.get(key);
        return (el == null || el.isJsonNull()) ? null : el.getAsInt();
    }

    public static int getInt(JsonObject obj, String key, int fallback) {
        Integer v = getIntOrNull(obj, key);
        return v == null ? fallback : v;
    }

    public static boolean getBool(JsonObject obj, String key, boolean fallback) {
        if (obj == null) return fallback;
        JsonElement el = obj.get(key);
        return (el == null || el.isJsonNull()) ? fallback : el.getAsBoolean();
    }

    /** Reads a JSON string array into a Java list (empty when absent). */
    public static List<String> getStringList(JsonObject obj, String key) {
        List<String> out = new ArrayList<>();
        if (obj == null) return out;
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonArray()) {
            for (JsonElement e : el.getAsJsonArray()) {
                if (!e.isJsonNull()) {
                    out.add(e.getAsString());
                }
            }
        }
        return out;
    }

    /** Serializes any object to a JsonElement using the shared Gson instance. */
    public static JsonElement tree(Object value) {
        return GSON.toJsonTree(value);
    }

    /** Serializes a list to a JsonArray. */
    public static JsonArray array(Object list) {
        return GSON.toJsonTree(list).getAsJsonArray();
    }
}
