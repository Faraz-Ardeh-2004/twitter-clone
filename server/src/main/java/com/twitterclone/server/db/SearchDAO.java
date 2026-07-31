package com.twitterclone.server.db;

import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple search using ILIKE (case-insensitive pattern matching in Postgres).
 * Fine for this project's data volume - if search gets slow later, pg_trgm
 * indexes are the next step (see the commented-out section in schema.sql).
 */
public class SearchDAO {

    /** Finds users whose username contains the query string (case-insensitive). */
    public List<User> searchUsers(String query) throws SQLException {
        String sql = "SELECT id, username, email, password_hash FROM users " +
                "WHERE username ILIKE ? ORDER BY username ASC";
        List<User> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    results.add(u);
                }
            }
        }
        return results;
    }

    /** Finds tweets whose content contains the query string, newest first. */
    public List<Tweet> searchTweets(String query) throws SQLException {
        String sql = "SELECT t.id, t.author_id, t.content, t.created_at, t.parent_tweet_id, " +
                "       u.username AS author_username, " +
                "       (SELECT COUNT(*) FROM likes l WHERE l.tweet_id = t.id) AS like_count " +
                "FROM tweets t JOIN users u ON t.author_id = u.id " +
                "WHERE t.content ILIKE ? " +
                "ORDER BY t.created_at DESC";
        List<Tweet> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
                    results.add(t);
                }
            }
        }
        return results;
    }
}