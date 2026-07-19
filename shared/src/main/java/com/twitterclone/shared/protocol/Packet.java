package com.twitterclone.shared.protocol;

import com.google.gson.JsonObject;

/**
 * ============================================================
 * Owner: all 3 people together (Phase 0 - Day 1), read-only after that
 * ============================================================
 * This class is the "packet" / message unit exchanged between client and
 * server. Both Hesam (server) and Faraz (client) use this shared class so
 * both sides build and read the same JSON shape.
 *
 * Shape on the wire (after Gson.toJson) looks like this:
 * {
 *   "type": "LOGIN",
 *   "token": null,
 *   "status": null,
 *   "message": null,
 *   "payload": { "username": "faraz", "password": "12345678" }
 * }
 *
 * Important protocol note: every message is sent as one complete line,
 * because both client and server read with BufferedReader.readLine().
 * So never let a newline character end up inside the JSON string (Gson
 * produces single-line JSON by default as long as you don't pretty-print it,
 * so a plain println is fine).
 */
public class Packet {

    private String type;      // one of the PacketType values (stored as a String)
    private String token;     // session token; empty for REGISTER/LOGIN
    private String status;    // "OK" or "ERROR" (only used in server responses)
    private String message;   // error message or short description (optional)
    private JsonObject payload; // the main body of the message (fields specific to each type)

    public Packet() {
        // Empty constructor required so Gson can use it during deserialization
    }

    public Packet(String type, String token, String status, String message, JsonObject payload) {
        this.type = type;
        this.token = token;
        this.status = status;
        this.message = message;
        this.payload = payload;
    }

    // ---------- Factory helpers (for convenience in Handler/NetworkService code) ----------

    /** Builds a request packet sent from the client to the server. */
    public static Packet request(PacketType type, String token, JsonObject payload) {
        return new Packet(type.name(), token, null, null, payload);
    }

    /** Builds a successful response packet sent from the server. */
    public static Packet ok(PacketType type, JsonObject payload) {
        return new Packet(type.name(), null, "OK", null, payload);
    }

    /** Builds an error response packet sent from the server. */
    public static Packet error(PacketType type, String message) {
        return new Packet(type.name(), null, "ERROR", message, null);
    }

    /** Builds a push packet (unsolicited, e.g. NEW_TWEET_PUSH). */
    public static Packet push(PacketType type, JsonObject payload) {
        return new Packet(type.name(), null, "OK", null, payload);
    }

    // ---------- Getters / Setters ----------

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Packet{type=" + type + ", token=" + token + ", status=" + status
                + ", message=" + message + ", payload=" + payload + "}";
    }
}
