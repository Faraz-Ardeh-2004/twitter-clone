package com.twitterclone.shared.model;

/**
 * ============================================================
 * Shared user model (POJO — no database logic here; that lives in UserDAO).
 * ============================================================
 * Used on both sides: the server fills it from the DB, Gson serializes it over
 * the socket, and the client reads it to render profiles/cards.
 *
 * The profile fields (displayName, bio, avatarUrl, bannerUrl) and the
 * aggregate stats (followerCount, followingCount, tweetCount, following) are
 * populated by UserDAO via JOIN/COUNT queries when a full profile is
 * requested; for lightweight results (e.g. search) only the basic identity
 * fields need to be set.
 */
public class User {

    private int id;
    private String username;
    private String email;
    private String displayName;
    private String bio;
    private String avatarUrl;   // base64 data URI or a URL
    private String bannerUrl;
    private boolean verified;

    // --- aggregate stats (not columns on users; filled for profile views) ---
    private int followerCount;
    private int followingCount;
    private int tweetCount;

    /** True when the user viewing this profile currently follows this user. */
    private boolean following;

    /**
     * WARNING: server-only. Never send a populated password hash to a client.
     * It is marked transient so Gson omits it during serialization even if set.
     */
    private transient String passwordHash;

    public User() {
    }

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getTweetCount() {
        return tweetCount;
    }

    public void setTweetCount(int tweetCount) {
        this.tweetCount = tweetCount;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** Convenience: display name if set, otherwise the username. */
    public String displayNameOrUsername() {
        return (displayName == null || displayName.isBlank()) ? username : displayName;
    }
}
