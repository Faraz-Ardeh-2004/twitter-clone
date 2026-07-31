package com.twitterclone.shared.model;

/**
 * Shared notification model (POJO). Persisted/queried by NotificationDAO and
 * pushed in real time via NOTIFICATION_PUSH.
 *
 * type is one of: FOLLOW, LIKE, REPLY, RETWEET. actorUsername is who triggered
 * it; tweetId is optional context (the tweet that was liked/replied/retweeted).
 */
public class Notification {

    private int id;
    private int userId;         // recipient
    private int actorId;        // who triggered it
    private String actorUsername;
    private String type;
    private Integer tweetId;    // optional
    private boolean read;
    private String createdAt;   // ISO-8601 string

    public Notification() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTweetId() {
        return tweetId;
    }

    public void setTweetId(Integer tweetId) {
        this.tweetId = tweetId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** Human-readable summary for the notifications list. */
    public String describe() {
        String actor = "@" + actorUsername;
        switch (type == null ? "" : type) {
            case "FOLLOW":  return actor + " followed you";
            case "LIKE":    return actor + " liked your tweet";
            case "REPLY":   return actor + " replied to your tweet";
            case "RETWEET": return actor + " retweeted your tweet";
            default:        return actor + " interacted with you";
        }
    }
}
