package com.twitterclone.shared.protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.UUID;

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
    private String requestId; // correlates a response to its request; null for pushes
    private String token;     // session token; empty for REGISTER/LOGIN
    private String status;    // "OK" or "ERROR" (only used in server responses)
    private String message;   // error message or short description (optional)
    // Stored as JsonElement (not JsonObject) so an explicit "payload": null on
    // the wire deserializes cleanly to JsonNull instead of throwing. The getter
    // still exposes it as a JsonObject (or null) so callers are unaffected.
    private JsonElement payload;

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

    /**
     * Builds a request packet sent from the client to the server. Each request
     * gets a unique requestId so the client can match the eventual response to
     * this exact call, even if several requests of the same type are in flight.
     */
    public static Packet request(PacketType type, String token, JsonObject payload) {
        Packet p = new Packet(type.name(), token, null, null, payload);
        p.requestId = UUID.randomUUID().toString();
        return p;
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
        return (payload == null || payload.isJsonNull()) ? null : payload.getAsJsonObject();
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
