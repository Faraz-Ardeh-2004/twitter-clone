package com.twitterclone.client.network;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 1 - Day 3-5
 * ============================================================
 * A simple holder for the logged-in user's state (token + basic info) in the
 * client's memory. After a successful login, LoginController populates this
 * class; the rest of the controllers (Dashboard, TweetCard, ...) read the
 * token/username from here.
 *
 * TODO(Faraz):
 *  1. In LoginController, after a successful LOGIN response:
 *       UserContext.getInstance().setSession(token, userId, username);
 *  2. Anywhere a request needs a token, use UserContext.getInstance().getToken().
 *  3. On logout (if you implement it), call UserContext.getInstance().clear().
 */
public class UserContext {

    private static final UserContext INSTANCE = new UserContext();

    private String token;
    private int userId;
    private String username;

    private UserContext() {
    }

    public static UserContext getInstance() {
        return INSTANCE;
    }

    public void setSession(String token, int userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }

    public void clear() {
        this.token = null;
        this.userId = 0;
        this.username = null;
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
