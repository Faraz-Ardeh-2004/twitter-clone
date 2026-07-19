package com.twitterclone.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 0 (base skeleton) -> Phase 2 (listening for pushes)
 * ============================================================
 * Singleton that keeps the main socket open and has a background thread that
 * continuously waits for incoming messages from the server (whether they're
 * responses to requests or real-time pushes like NEW_TWEET_PUSH).
 *
 * Usage pattern from controllers:
 *   NetworkService.getInstance().sendRequest(
 *       Packet.request(PacketType.LOGIN, null, payload),
 *       response -> { ... whatever needs to happen with the response, called on the UI thread ... }
 *   );
 *
 * WARNING: this design is intentionally simple - each callback is matched by
 * message "type", not by a dedicated requestId (the current protocol doesn't
 * have such a field). This means if you send two requests of the same type
 * at once, whichever response arrives first will trigger the callback. For
 * this project's sequential flows (a user submits one form and waits for its
 * answer) this is enough; if you need something more robust later, talk to
 * Hesam first since it would require adding a new field (e.g. requestId) to
 * the shared Packet class.
 */
public class NetworkService {

    private static final NetworkService INSTANCE = new NetworkService();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private volatile boolean running = false;

    private final Gson gson = new Gson();

    // message type (String) -> callback waiting for a response of that type
    private final Map<String, Consumer<Packet>> pendingCallbacks = new ConcurrentHashMap<>();

    // global callback for push messages (unsolicited), e.g. NEW_TWEET_PUSH
    private volatile Consumer<Packet> pushListener;

    private NetworkService() {
    }

    public static NetworkService getInstance() {
        return INSTANCE;
    }

    /** Initial connection to the server. Should only be called once (in ClientMain.start). */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        running = true;
        listenerThread = new Thread(this::listenLoop, "network-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Sends a request to the server and registers a callback for when the
     * response of the same type arrives.
     *
     * TODO(Faraz): before sending, if request.getToken() == null and
     * UserContext has a valid token, set it (for requests that require login):
     *   request.setToken(UserContext.getInstance().getToken());
     */
    public void sendRequest(Packet request, Consumer<Packet> onResponse) {
        pendingCallbacks.put(request.getType(), onResponse);
        out.println(gson.toJson(request));
    }

    /** Registers a global listener for push messages (e.g. NEW_TWEET_PUSH). Called only once, in DashboardController. */
    public void setPushListener(Consumer<Packet> listener) {
        this.pushListener = listener;
    }

    /**
     * TODO(Faraz - Phase 0 initial test, completed in Phase 2): the main loop
     * of the background thread. Reads every incoming line, converts it to a
     * Packet, and decides based on its type whether to call a waiting
     * callback or treat it as a push.
     *
     * WARNING - critical JavaFX note: this method runs on a thread other than
     * the JavaFX Application Thread. Never update the UI directly from here;
     * always use Platform.runLater(...) (already wired in below).
     */
    private void listenLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                Packet packet;
                try {
                    packet = gson.fromJson(line, Packet.class);
                } catch (JsonSyntaxException e) {
                    continue; // TODO(Faraz): log if needed, but don't crash
                }

                if (packet == null || packet.getType() == null) {
                    continue;
                }

                Packet finalPacket = packet;

                if (PacketType.NEW_TWEET_PUSH.name().equals(packet.getType())) {
                    // TODO(Faraz - Phase 2): this is a push, not a response to a request.
                    if (pushListener != null) {
                        Platform.runLater(() -> pushListener.accept(finalPacket));
                    }
                    continue;
                }

                Consumer<Packet> callback = pendingCallbacks.remove(packet.getType());
                if (callback != null) {
                    Platform.runLater(() -> callback.accept(finalPacket));
                }
                // TODO(Faraz): if no callback was found (unexpected message), you can log it.
            }
        } catch (IOException e) {
            System.out.println("Connection to server lost: " + e.getMessage());
        }
    }
}
