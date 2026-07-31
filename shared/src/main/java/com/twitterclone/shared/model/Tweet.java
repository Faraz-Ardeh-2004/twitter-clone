package com.twitterclone.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Shared tweet model (POJO). Save/read logic lives in TweetDAO.
 * ============================================================
 * A single Tweet instance may represent a plain tweet, a reply
 * (parentTweetId set), or — as it appears in a feed — a retweet. For a retweet
 * the row is "flattened" for display: the id/author/content fields hold the
 * ORIGINAL tweet's data (so liking/replying targets the original), while
 * retweetOf and retweetedBy mark that this feed entry is a repost.
 *
 * The aggregate fields (likeCount, replyCount, retweetCount) and the
 * per-viewer flags (liked, retweeted) are computed by TweetDAO at query time.
 */
public class Tweet {

    private int id;
    private int authorId;
    private String authorUsername;      // from JOIN with users
    private String authorDisplayName;   // from JOIN with users
    private String authorAvatarUrl;     // from JOIN with users
    private String content;
    private String createdAt;           // ISO-8601 string

    private Integer parentTweetId;      // non-null when this tweet is a reply

    // --- retweet metadata (populated when this feed entry is a repost) ---
    private Integer retweetOf;          // original tweet id
    private String retweetedBy;         // username of the account that reposted

    // --- media ---
    private List<String> media = new ArrayList<>(); // base64 data URIs

    // --- hashtags parsed from content ---
    private List<String> hashtags = new ArrayList<>();

    // --- aggregate counts ---
    private int likeCount;
    private int replyCount;
    private int retweetCount;

    // --- per-viewer flags (relative to the requesting user) ---
    private boolean liked;
    private boolean retweeted;

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

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
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

    public Integer getRetweetOf() {
        return retweetOf;
    }

    public void setRetweetOf(Integer retweetOf) {
        this.retweetOf = retweetOf;
    }

    public String getRetweetedBy() {
        return retweetedBy;
    }

    public void setRetweetedBy(String retweetedBy) {
        this.retweetedBy = retweetedBy;
    }

    public List<String> getMedia() {
        return media;
    }

    public void setMedia(List<String> media) {
        this.media = (media == null) ? new ArrayList<>() : media;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = (hashtags == null) ? new ArrayList<>() : hashtags;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }
}
