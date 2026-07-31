package com.twitterclone.server.network;

import com.twitterclone.server.db.FollowDAO;
import com.twitterclone.shared.protocol.Packet;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 2 - Day 6-8
 * ============================================================
 * The heart of the project's real-time mechanism. Keeps a static map from
 * userId to the active ClientHandler for that user. When a new tweet is
 * created (TweetHandler), this class is used to find online clients and
 * send them a NEW_TWEET_PUSH.
 *
 * WARNING: several threads (one per client) access this class concurrently,
 * so it needs a thread-safe data structure; that's why ConcurrentHashMap was
 * chosen instead of a plain HashMap.
 *
 * TODO(Hesam):
 *  1. In AuthHandler, right after a successful LOGIN, call: register(userId, clientHandler)
 *  2. In ClientHandler.run() (the finally block, on disconnect), call: unregister(userId)
 *  3. In TweetHandler, after saving a new tweet, use broadcastToAll or broadcastToFollowers.
 */
public class ConnectionRegistry {

    private static final ConcurrentMap<Integer, ClientHandler> ONLINE_USERS = new ConcurrentHashMap<>();
    private static final FollowDAO followDAO = new FollowDAO();

    private ConnectionRegistry() {
        // utility class - should not be instantiated
    }

    public static void register(int userId, ClientHandler handler) {
        ONLINE_USERS.put(userId, handler);
    }

    public static void unregister(int userId) {
        ONLINE_USERS.remove(userId);
    }

    public static ClientHandler get(int userId) {
        return ONLINE_USERS.get(userId);
    }

    public static boolean isOnline(int userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    /**
     * TODO(Hesam - Phase 2, simple version): sends the packet to *all* online
     * users. Per the spec, this simple version is fine to start with in
     * Phase 2 for speed.
     */
    public static void broadcastToAll(Packet packet) {
        for (ClientHandler handler : ONLINE_USERS.values()) {
            handler.sendPacket(packet);
        }
    }

    /** Sends a packet to a single user if they are currently online. */
    public static void sendTo(int userId, Packet packet) {
        ClientHandler handler = ONLINE_USERS.get(userId);
        if (handler != null) {
            handler.sendPacket(packet);
        }
    }

    /**
     * Real-time delivery: sends the packet to the author and to every follower
     * of the author who is currently online. This is what makes newly published
     * tweets appear in followers' feeds without a manual refresh.
     */
    public static void broadcastToFollowers(int authorId, Packet packet) {
        sendTo(authorId, packet);
        try {
            List<Integer> followerIds = followDAO.getFollowerIds(authorId);
            for (int followerId : followerIds) {
                sendTo(followerId, packet);
            }
        } catch (SQLException e) {
            System.err.println("broadcastToFollowers failed: " + e.getMessage());
        }
    }

    /** Useful for debugging / the Phase 4 concurrency test. */
    public static Collection<ClientHandler> getAllOnline() {
        return ONLINE_USERS.values();
    }
}
