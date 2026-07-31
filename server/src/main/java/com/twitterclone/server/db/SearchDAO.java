package com.twitterclone.server.db;

import com.twitterclone.shared.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User search using ILIKE (case-insensitive). Matches on username OR display
 * name, and annotates each result with the viewer's follow state and the
 * user's follower count. Tweet/hashtag search lives in TweetDAO so results
 * share the same viewer-aware shape (liked/retweeted flags, counts).
 */
public class SearchDAO {

    public List<User> searchUsers(String query, int viewerId) throws SQLException {
        String sql = "SELECT u.id, u.username, u.display_name, u.avatar_url, u.bio, u.verified, " +
                "  (SELECT COUNT(*) FROM follows f WHERE f.following_id = u.id) AS followers, " +
                "  EXISTS(SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = u.id) AS is_following " +
                "FROM users u " +
                "WHERE u.username ILIKE ? OR u.display_name ILIKE ? " +
                "ORDER BY followers DESC, u.username ASC LIMIT 100";
        List<User> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, viewerId);
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setDisplayName(rs.getString("display_name"));
                    u.setAvatarUrl(rs.getString("avatar_url"));
                    u.setBio(rs.getString("bio"));
                    u.setVerified(rs.getBoolean("verified"));
                    u.setFollowerCount(rs.getInt("followers"));
                    u.setFollowing(rs.getBoolean("is_following"));
                    results.add(u);
                }
            }
        }
        return results;
    }
}
