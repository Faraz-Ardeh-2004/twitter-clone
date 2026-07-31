package com.twitterclone.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * follows table: composite key (follower_id, following_id).
 */
public class FollowDAO {

    /** Inserts the relationship; if it already exists, does nothing (idempotent). */
    public boolean follow(int followerId, int followingId) throws SQLException {
        String sql = "INSERT INTO follows (follower_id, following_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean unfollow(int followerId, int followingId) throws SQLException {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND following_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Ids of everyone who follows userId. */
    public List<Integer> getFollowerIds(int userId) throws SQLException {
        return idList("SELECT follower_id FROM follows WHERE following_id = ?", userId);
    }

    /** Ids of everyone userId follows. */
    public List<Integer> getFollowingIds(int userId) throws SQLException {
        return idList("SELECT following_id FROM follows WHERE follower_id = ?", userId);
    }

    public boolean isFollowing(int followerId, int followingId) throws SQLException {
        String sql = "SELECT 1 FROM follows WHERE follower_id = ? AND following_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countFollowers(int userId) throws SQLException {
        return count("SELECT COUNT(*) FROM follows WHERE following_id = ?", userId);
    }

    public int countFollowing(int userId) throws SQLException {
        return count("SELECT COUNT(*) FROM follows WHERE follower_id = ?", userId);
    }

    // --- helpers ---

    private List<Integer> idList(String sql, int param) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    private int count(String sql, int param) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}