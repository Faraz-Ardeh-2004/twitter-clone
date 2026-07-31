package com.twitterclone.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.twitterclone.shared.protocol.Packet;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Singleton network layer for the client. Keeps one socket open for the whole
 * app and runs a background listener thread that reads every incoming line.
 *
 * Correlation model: each request carries a unique requestId (set by
 * Packet.request). A response echoes that id, so its callback is matched
 * exactly — even if several requests of the same type are in flight. Messages
 * with no requestId are unsolicited server pushes (NEW_TWEET_PUSH, LIKE_PUSH,
 * FOLLOW_PUSH, NOTIFICATION_PUSH); those are delivered to listeners registered
 * per packet type. All callbacks run on the JavaFX Application Thread.
 */
public class NetworkService {

    private static final NetworkService INSTANCE = new NetworkService();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private volatile boolean running = false;

    private final Gson gson = new Gson();

    // requestId -> callback awaiting that specific response
    private final Map<String, Consumer<Packet>> pendingCallbacks = new ConcurrentHashMap<>();

    // push packet type -> listeners interested in it
    private final Map<String, List<Consumer<Packet>>> pushListeners = new ConcurrentHashMap<>();

    // invoked (on the FX thread) if the connection to the server drops
    private volatile Runnable onDisconnect;

    private NetworkService() {
    }

    public static NetworkService getInstance() {
        return INSTANCE;
    }

    /** Opens the connection. Call once at app start. */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        running = true;
        listenerThread = new Thread(this::listenLoop, "network-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    /** Sends a request and registers a callback for its matching response. */
    public void sendRequest(Packet request, Consumer<Packet> onResponse) {
        // Attach the session token automatically unless the caller set one.
        if (request.getToken() == null && UserContext.getInstance().isLoggedIn()) {
            request.setToken(UserContext.getInstance().getToken());
        }
        if (onResponse != null && request.getRequestId() != null) {
            pendingCallbacks.put(request.getRequestId(), onResponse);
        }
        synchronized (this) {
            out.println(gson.toJson(request));
        }
    }

    /** Registers a listener for a given push packet type (e.g. NEW_TWEET_PUSH). */
    public void addPushListener(String type, Consumer<Packet> listener) {
        pushListeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void removePushListener(String type, Consumer<Packet> listener) {
        List<Consumer<Packet>> list = pushListeners.get(type);
        if (list != null) {
            list.remove(listener);
        }
    }

    /** Drops all push listeners and pending callbacks (used on logout). */
    public void clearListeners() {
        pushListeners.clear();
        pendingCallbacks.clear();
    }

    public void setOnDisconnect(Runnable handler) {
        this.onDisconnect = handler;
    }

    private void listenLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                Packet packet;
                try {
                    packet = gson.fromJson(line, Packet.class);
                } catch (JsonSyntaxException e) {
                    continue;
                }
                if (packet == null || packet.getType() == null) {
                    continue;
                }

                final Packet p = packet;
                String requestId = packet.getRequestId();
                if (requestId != null) {
                    Consumer<Packet> callback = pendingCallbacks.remove(requestId);
                    if (callback != null) {
                        Platform.runLater(() -> callback.accept(p));
                        continue;
                    }
                }
                // Otherwise it's a push: fan out to listeners for this type.
                List<Consumer<Packet>> listeners = pushListeners.get(packet.getType());
                if (listeners != null) {
                    for (Consumer<Packet> l : listeners) {
                        Platform.runLater(() -> l.accept(p));
                    }
                }
            }
        } catch (IOException e) {
            // fall through to disconnect handling
        }
        if (running && onDisconnect != null) {
            Platform.runLater(onDisconnect);
        }
    }
}
