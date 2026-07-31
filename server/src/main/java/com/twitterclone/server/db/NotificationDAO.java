package com.twitterclone.server.db;

import com.twitterclone.shared.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Access to the {@code notifications} table.
 */
public class NotificationDAO {

    /**
     * Creates a notification for {@code userId} triggered by {@code actorId}.
     * Self-actions (actor == recipient) produce nothing and return null, so a
     * user is never notified about their own likes/replies. The returned
     * Notification is fully populated (including the actor's username) so the
     * caller can push it in real time.
     */
    public Notification create(int userId, int actorId, String type, Integer tweetId) throws SQLException {
        if (userId == actorId) {
            return null;
        }
        String sql = "INSERT INTO notifications (user_id, actor_id, type, tweet_id) " +
                "VALUES (?, ?, ?, ?) RETURNING id, created_at";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, actorId);
            stmt.setString(3, type);
            if (tweetId != null) {
                stmt.setInt(4, tweetId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(userId);
                    n.setActorId(actorId);
                    n.setType(type);
                    n.setTweetId(tweetId);
                    n.setRead(false);
                    n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                    n.setActorUsername(lookupUsername(conn, actorId));
                    return n;
                }
            }
        }
        return null;
    }

    /** Removes a prior notification (e.g. when an action is undone: unlike/unfollow). */
    public void delete(int userId, int actorId, String type, Integer tweetId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ? AND actor_id = ? AND type = ? " +
                "AND tweet_id IS NOT DISTINCT FROM ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, actorId);
            stmt.setString(3, type);
            if (tweetId != null) {
                stmt.setInt(4, tweetId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    public List<Notification> list(int userId, int limit) throws SQLException {
        String sql = "SELECT n.id, n.user_id, n.actor_id, n.type, n.tweet_id, n.is_read, n.created_at, " +
                "       a.username AS actor_username " +
                "FROM notifications n JOIN users a ON n.actor_id = a.id " +
                "WHERE n.user_id = ? ORDER BY n.created_at DESC LIMIT ?";
        List<Notification> out = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(rs.getInt("user_id"));
                    n.setActorId(rs.getInt("actor_id"));
                    n.setType(rs.getString("type"));
                    int t = rs.getInt("tweet_id");
                    n.setTweetId(rs.wasNull() ? null : t);
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                    n.setActorUsername(rs.getString("actor_username"));
                    out.add(n);
                }
            }
        }
        return out;
    }

    public int unreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void markAllRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private String lookupUsername(Connection conn, int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("username") : null;
            }
        }
    }
}
