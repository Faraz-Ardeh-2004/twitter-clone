package com.twitterclone.server.db;

import com.twitterclone.shared.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL access for the {@code users} table. Handlers call these methods and
 * never write raw SQL themselves. Every statement is parameterized.
 */
public class UserDAO {

    private static final String COLS =
            "id, username, email, password_hash, display_name, bio, avatar_url, banner_url, verified";

    /**
     * Saves a new user. Assumes the password is already hashed by the caller.
     * The display name defaults to the username when not provided.
     */
    public User insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, display_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            String display = (user.getDisplayName() == null || user.getDisplayName().isBlank())
                    ? user.getUsername() : user.getDisplayName();
            stmt.setString(4, display);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            user.setDisplayName(display);
        }
        return user;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT " + COLS + " FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT " + COLS + " FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        return exists("SELECT 1 FROM users WHERE username = ?", username);
    }

    public boolean emailExists(String email) throws SQLException {
        return exists("SELECT 1 FROM users WHERE email = ?", email);
    }

    /**
     * Full profile for {@code profileUserId} as seen by {@code viewerId}:
     * includes follower/following/tweet counts and whether the viewer follows
     * this user. Never returns the password hash to callers that serialize it
     * (the field is transient anyway).
     */
    public User getProfile(int profileUserId, int viewerId) throws SQLException {
        String sql = "SELECT " +
                "  u.id, u.username, u.email, u.display_name, u.bio, u.avatar_url, u.banner_url, u.verified, " +
                "  (SELECT COUNT(*) FROM follows f WHERE f.following_id = u.id) AS followers, " +
                "  (SELECT COUNT(*) FROM follows f WHERE f.follower_id  = u.id) AS following, " +
                "  (SELECT COUNT(*) FROM tweets t WHERE t.author_id = u.id AND t.parent_tweet_id IS NULL) AS tweets, " +
                "  EXISTS(SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = u.id) AS is_following " +
                "FROM users u WHERE u.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, viewerId);
            stmt.setInt(2, profileUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setDisplayName(rs.getString("display_name"));
                u.setBio(rs.getString("bio"));
                u.setAvatarUrl(rs.getString("avatar_url"));
                u.setBannerUrl(rs.getString("banner_url"));
                u.setVerified(rs.getBoolean("verified"));
                u.setFollowerCount(rs.getInt("followers"));
                u.setFollowingCount(rs.getInt("following"));
                u.setTweetCount(rs.getInt("tweets"));
                u.setFollowing(rs.getBoolean("is_following"));
                return u;
            }
        }
    }

    /**
     * Updates the profile fields. Only non-null arguments are applied, so a
     * caller can update just the bio without clearing the avatar, etc.
     */
    public void updateProfile(int userId, String displayName, String bio,
                              String avatarUrl, String bannerUrl) throws SQLException {
        List<String> sets = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (displayName != null) { sets.add("display_name = ?"); args.add(displayName); }
        if (bio != null)         { sets.add("bio = ?");          args.add(bio); }
        if (avatarUrl != null)   { sets.add("avatar_url = ?");   args.add(avatarUrl); }
        if (bannerUrl != null)   { sets.add("banner_url = ?");   args.add(bannerUrl); }
        if (sets.isEmpty()) {
            return;
        }
        String sql = "UPDATE users SET " + String.join(", ", sets) + " WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object a : args) {
                stmt.setString(i++, (String) a);
            }
            stmt.setInt(i, userId);
            stmt.executeUpdate();
        }
    }

    /** Users who follow {@code userId}, annotated with the viewer's follow state. */
    public List<User> getFollowers(int userId, int viewerId) throws SQLException {
        String sql = "SELECT " + profileListCols() +
                "FROM follows fo JOIN users u ON fo.follower_id = u.id " +
                "WHERE fo.following_id = ? ORDER BY fo.created_at DESC";
        return profileList(sql, userId, viewerId);
    }

    /** Users that {@code userId} follows, annotated with the viewer's follow state. */
    public List<User> getFollowing(int userId, int viewerId) throws SQLException {
        String sql = "SELECT " + profileListCols() +
                "FROM follows fo JOIN users u ON fo.following_id = u.id " +
                "WHERE fo.follower_id = ? ORDER BY fo.created_at DESC";
        return profileList(sql, userId, viewerId);
    }

    /** Suggested users to follow: accounts the viewer doesn't already follow, by follower count. */
    public List<User> suggestions(int viewerId, int limit) throws SQLException {
        String sql = "SELECT " +
                "  u.id, u.username, u.display_name, u.avatar_url, u.bio, u.verified, " +
                "  (SELECT COUNT(*) FROM follows f WHERE f.following_id = u.id) AS followers, " +
                "  FALSE AS is_following " +
                "FROM users u " +
                "WHERE u.id <> ? " +
                "  AND NOT EXISTS (SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = u.id) " +
                "ORDER BY followers DESC, u.id ASC LIMIT ?";
        List<User> out = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, viewerId);
            stmt.setInt(2, viewerId);
            stmt.setInt(3, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(mapProfileListRow(rs));
                }
            }
        }
        return out;
    }

    // --- helpers ---

    private String profileListCols() {
        return "  u.id, u.username, u.display_name, u.avatar_url, u.bio, u.verified, " +
               "  (SELECT COUNT(*) FROM follows f WHERE f.following_id = u.id) AS followers, " +
               "  EXISTS(SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = u.id) AS is_following ";
    }

    private List<User> profileList(String sql, int userId, int viewerId) throws SQLException {
        List<User> out = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, viewerId); // for the EXISTS in profileListCols
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(mapProfileListRow(rs));
                }
            }
        }
        return out;
    }

    private User mapProfileListRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setDisplayName(rs.getString("display_name"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setBio(rs.getString("bio"));
        u.setVerified(rs.getBoolean("verified"));
        u.setFollowerCount(rs.getInt("followers"));
        u.setFollowing(rs.getBoolean("is_following"));
        return u;
    }

    private boolean exists(String sql, String param) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setDisplayName(rs.getString("display_name"));
        u.setBio(rs.getString("bio"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setBannerUrl(rs.getString("banner_url"));
        u.setVerified(rs.getBoolean("verified"));
        return u;
    }
}
