package com.twitterclone.server.auth;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 1 - Day 3-5
 * ============================================================
 * The authentication middleware layer. Responsible for generating and
 * validating session tokens.
 *
 * The spec offers two options for storing sessions: either in the database
 * (a sessions table that AmirAli would create) or in an in-memory
 * ConcurrentHashMap. For speed within the 2 weeks, the in-memory version
 * below is recommended; if you need real expiry later, you can add the
 * sessions table too.
 *
 * TODO(Hesam):
 *  1. In AuthHandler.handleLogin, after successfully verifying the password: createSession(userId)
 *  2. In Dispatcher, before any operation that requires login: validate(token)
 *  3. In AuthHandler.handleLogout: invalidate(token)
 */
public class Authenticator {

    // token -> userId
    private static final ConcurrentMap<String, Integer> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    private Authenticator() {
    }

    /** Creates a new session token for userId and returns it. */
    public static String createSession(int userId) {
        String token = UUID.randomUUID().toString();
        ACTIVE_SESSIONS.put(token, userId);
        return token;
    }

    /**
     * Validates the token and returns the userId if valid, otherwise null.
     * TODO(Hesam): call this in Dispatcher for every protected endpoint.
     */
    public static Integer validate(String token) {
        if (token == null) return null;
        return ACTIVE_SESSIONS.get(token);
    }

    public static void invalidate(String token) {
        if (token != null) {
            ACTIVE_SESSIONS.remove(token);
        }
    }
}
