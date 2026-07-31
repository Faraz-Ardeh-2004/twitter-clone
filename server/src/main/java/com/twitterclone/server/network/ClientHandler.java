package com.twitterclone.server.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.twitterclone.server.auth.Authenticator;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 0 - Day 1-2 (skeleton) + Phase 2 (register with ConnectionRegistry)
 * ============================================================
 * One instance of this class represents "one connected client" and runs on
 * its own thread from the thread pool (see ServerMain). This class:
 *   - reads line-by-line messages from the client (since the client also
 *     uses BufferedReader/PrintWriter)
 *   - converts each line into a Packet using Gson
 *   - hands the Packet to the Dispatcher so the right handler gets called
 *   - sends the Dispatcher's response back to the client
 *
 * This class also holds the userId of whoever is logged in on this
 * connection; that's exactly what ConnectionRegistry needs for real-time
 * push (NEW_TWEET_PUSH).
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Gson gson = new Gson();

    private BufferedReader in;
    private PrintWriter out;

    // TODO(Hesam - Phase 1): once the user logs in successfully, populate this
    // field in AuthHandler (setUserId) and register it right here with
    // ConnectionRegistry.register(userId, this).
    private Integer userId = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            String line;
            // TODO(Hesam): main message-reading loop. Keep going while the client is connected.
            while ((line = in.readLine()) != null) {
                Packet response = handleLine(line);
                if (response != null) {
                    sendPacket(response);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            // If the user was logged in, remove them from the registry so no one
            // tries to push to a closed socket.
            if (userId != null) {
                ConnectionRegistry.unregister(userId);
            }
            closeQuietly();
        }
    }

    /** Parses one raw JSON line, dispatches it, and returns the response packet. */
    private Packet handleLine(String line) {
        Packet request;
        try {
            request = gson.fromJson(line, Packet.class);
        } catch (JsonSyntaxException e) {
            return Packet.error(PacketType.ERROR, "Invalid JSON: " + e.getMessage());
        }

        if (request == null || request.getType() == null) {
            return Packet.error(PacketType.ERROR, "Missing 'type' field");
        }

        Packet response;
        try {
            response = Dispatcher.dispatch(this, request);
        } catch (RuntimeException e) {
            // A handler bug must never take down this client's thread.
            System.err.println("Dispatch error for " + request.getType() + ": " + e);
            response = Packet.error(PacketType.ERROR, "Server error handling " + request.getType());
        }
        // Echo the requestId so the client can match this response to its request.
        if (response != null) {
            response.setRequestId(request.getRequestId());
        }
        return response;
    }

    /** Sends a packet (response or push) to this specific client. Not thread-safe across callers; only one thread should call this per handler. */
    public synchronized void sendPacket(Packet packet) {
        // TODO(Hesam): make sure toJson produces a single line (Gson does this by
        // default, so a plain println is fine).
        out.println(gson.toJson(packet));
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
