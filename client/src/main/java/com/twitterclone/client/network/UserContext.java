package com.twitterclone.client.network;

/**
 * Holds the logged-in user's session state (token + identity) plus the client's
 * UI preferences (dark mode). Populated by LoginController after a successful
 * login and read by the rest of the controllers.
 */
public class UserContext {

    private static final UserContext INSTANCE = new UserContext();

    private String token;
    private int userId;
    private String username;
    private String displayName;
    private String avatarUrl;

    private boolean darkMode = false;

    private UserContext() {
    }

    public static UserContext getInstance() {
        return INSTANCE;
    }

    public void setSession(String token, int userId, String username, String displayName, String avatarUrl) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public void clear() {
        this.token = null;
        this.userId = 0;
        this.username = null;
        this.displayName = null;
        this.avatarUrl = null;
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

    public String getDisplayName() {
        return (displayName == null || displayName.isBlank()) ? username : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public void toggleDarkMode() {
        this.darkMode = !this.darkMode;
    }
}
