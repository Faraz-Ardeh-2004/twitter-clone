package com.twitterclone.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * likes table: composite key (user_id, tweet_id).
 * Uses INSERT ... ON CONFLICT DO NOTHING for like() so it stays atomic and
 * idempotent under concurrent requests (see the warning in the original TODO).
 */
public class LikeDAO {

    public boolean like(int userId, int tweetId) throws SQLException {
        String sql = "INSERT INTO likes (user_id, tweet_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, tweetId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean unlike(int userId, int tweetId) throws SQLException {
        String sql = "DELETE FROM likes WHERE user_id = ? AND tweet_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, tweetId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countLikes(int tweetId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE tweet_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tweetId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean hasLiked(int userId, int tweetId) throws SQLException {
        String sql = "SELECT 1 FROM likes WHERE user_id = ? AND tweet_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, tweetId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}