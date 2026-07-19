package com.twitterclone.shared.model;

/**
 * ============================================================
 * Primary owner: AmirAli (Database) | Phase 2 - Day 6-8
 * (Faraz uses this same class to render a TweetCard on the client)
 * ============================================================
 * Simple POJO for a tweet. Save/read logic lives in TweetDAO.
 *
 * TODO(AmirAli): in Phase 3, add the parentTweetId field (for replies).
 * TODO(AmirAli): for every tweet returned from GET_FEED, also populate
 *   authorUsername and likeCount (these come from a JOIN with
 *   users/likes, not from the tweets table itself; that's why they're kept
 *   separate from id/content here).
 */
public class Tweet {

    private int id;
    private int authorId;
    private String authorUsername; // result of a JOIN with the users table - for direct UI display
    private String content;
    private String createdAt; // ISO-8601 string; simpler than serializing java.time directly

    // TODO(AmirAli - Phase 3): use/enable this field once you add the
    // parent_tweet_id column to the tweets table. Leave it null if the tweet
    // is not a reply.
    private Integer parentTweetId;

    // TODO(AmirAli - Phase 3): populate the like count via a JOIN with the likes table.
    private int likeCount;

    public Tweet() {
    }

    public Tweet(int id, int authorId, String authorUsername, String content, String createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getParentTweetId() {
        return parentTweetId;
    }

    public void setParentTweetId(Integer parentTweetId) {
        this.parentTweetId = parentTweetId;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
