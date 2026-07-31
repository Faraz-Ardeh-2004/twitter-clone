package com.twitterclone.server.db;

import com.twitterclone.shared.model.Tweet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all SQL access related to the tweets table.
 * Every SELECT here joins with users to grab the author's username, and
 * uses a subquery to count likes - both fields live outside the tweets
 * table itself, but Tweet.java expects them populated for the client's UI.
 */
public class TweetDAO {

    // Shared SELECT clause - every read method below builds on top of this,
    // so the JOIN/like-count logic doesn't get duplicated everywhere.
    private static final String BASE_SELECT =
            "SELECT t.id, t.author_id, t.content, t.created_at, t.parent_tweet_id, " +
                    "       u.username AS author_username, " +
                    "       (SELECT COUNT(*) FROM likes l WHERE l.tweet_id = t.id) AS like_count " +
                    "FROM tweets t JOIN users u ON t.author_id = u.id ";

    /** Saves a new tweet and fills in the generated id + createdAt on the object. */
    public Tweet saveTweet(Tweet tweet) throws SQLException {
        String sql = "INSERT INTO tweets (author_id, content, created_at, parent_tweet_id) " +
                "VALUES (?, ?, now(), ?) RETURNING id, created_at";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tweet.getAuthorId());
            stmt.setString(2, tweet.getContent());
            if (tweet.getParentTweetId() != null) {
                stmt.setInt(3, tweet.getParentTweetId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    tweet.setId(rs.getInt("id"));
                    tweet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                }
            }
        }
        // Fresh tweet, no likes yet.
        tweet.setLikeCount(0);
        return tweet;
    }

    /**
     * Global feed - every tweet from every user, newest first.
     * Used by GET_FEED in Phase 2; replaced by getPersonalizedFeed in Phase 3.
     */
    public List<Tweet> getGlobalTweets(int limit, int offset) throws SQLException {
        String sql = BASE_SELECT + "ORDER BY t.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return runQuery(stmt);
        }
    }

    /**
     * The "critical" personalized feed: userId's own tweets + tweets from
     * everyone userId follows, newest first.
     */
    public List<Tweet> getPersonalizedFeed(int userId, int limit, int offset) throws SQLException {
        String sql = BASE_SELECT +
                "WHERE t.author_id = ? " +
                "   OR t.author_id IN (SELECT following_id FROM follows WHERE follower_id = ?) " +
                "ORDER BY t.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);
            return runQuery(stmt);
        }
    }

    /** All tweets by a specific user, for their profile page. */
    public List<Tweet> getUserTweets(int userId) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.author_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return runQuery(stmt);
        }
    }

    /** A single tweet by id, for the detail/reply page. */
    public Tweet getTweetById(int tweetId) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tweetId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /** All replies to a given tweet, oldest first (so a thread reads top to bottom). */
    public List<Tweet> getReplies(int parentTweetId) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.parent_tweet_id = ? ORDER BY t.created_at ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentTweetId);
            return runQuery(stmt);
        }
    }

    /**
     * Deletes a tweet, but only if authorId actually owns it.
     * Hesam should double-check ownership in the Handler too - don't rely
     * on this WHERE clause alone as the only safety net.
     */
    public boolean deleteTweet(int tweetId, int authorId) throws SQLException {
        String sql = "DELETE FROM tweets WHERE id = ? AND author_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tweetId);
            stmt.setInt(2, authorId);
            return stmt.executeUpdate() > 0;
        }
    }

    // --- helpers ---

    private List<Tweet> runQuery(PreparedStatement stmt) throws SQLException {
        List<Tweet> results = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    private Tweet mapRow(ResultSet rs) throws SQLException {
        Tweet t = new Tweet();
        t.setId(rs.getInt("id"));
        t.setAuthorId(rs.getInt("author_id"));
        t.setAuthorUsername(rs.getString("author_username"));
        t.setContent(rs.getString("content"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        t.setCreatedAt(createdAt.toLocalDateTime().toString());

        int parentId = rs.getInt("parent_tweet_id");
        t.setParentTweetId(rs.wasNull() ? null : parentId);

        t.setLikeCount(rs.getInt("like_count"));
        return t;
    }
}