package com.twitterclone.server.network;

import com.twitterclone.shared.protocol.Packet;

import java.util.Collection;
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

    /**
     * TODO(Hesam - Phase 3, once the follows table exists via AmirAli): send
     * the packet only to followers of authorId who are currently online, not
     * to everyone.
     * Suggested implementation:
     *   1. get followerIds via FollowDAO.getFollowerIds(authorId)
     *   2. for each id, if isOnline, call get(id).sendPacket(packet)
     */
    public static void broadcastToFollowers(int authorId, Packet packet) {
        throw new UnsupportedOperationException("TODO(Hesam): implement in Phase 3 once FollowDAO is ready");
    }

    /** Useful for debugging / the Phase 4 concurrency test. */
    public static Collection<ClientHandler> getAllOnline() {
        return ONLINE_USERS.values();
    }
}
