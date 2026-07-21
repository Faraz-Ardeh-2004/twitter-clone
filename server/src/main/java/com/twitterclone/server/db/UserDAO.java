package com.twitterclone.server.db;

import com.twitterclone.shared.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles all SQL access related to the users table.
 * Hesam (backend) only calls these methods and never writes raw SQL
 * directly inside his Handlers - everything goes through here.
 */
public class UserDAO {

    /**
     * Saves a new user to the database.
     * Note: this assumes the password is already hashed by the time it
     * gets here (i.e. PasswordUtil.hash(...) was called somewhere before
     * this). Never store a raw password directly.
     */
    public User insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.executeUpdate();

            // Grab the auto-generated id Postgres assigned and set it on
            // the User object, so the caller knows what id it got.
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        }
        return user;
    }

    /**
     * Finds a user by username.
     * Main use case is login: Hesam calls this in AuthHandler to fetch the
     * user, then compares the returned passwordHash using PasswordUtil.check(...).
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        // No match found - caller (e.g. AuthHandler) needs to handle null.
        return null;
    }

    /**
     * Finds a user by id.
     * Mostly useful when you only have an author_id from a tweet and need
     * to resolve it to an actual username/profile info.
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT id, username, email, password_hash FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Maps one row from the result set into a User object.
     * Pulled into its own method since the same mapping logic was getting
     * copy-pasted across the methods above.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        return u;
    }
}