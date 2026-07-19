package com.twitterclone.server.db;

import com.twitterclone.shared.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ============================================================
 * Owner: AmirAli (Database) | Phase 1 - Day 3-5
 * ============================================================
 * All SQL access related to the users table is centralized here. Hesam
 * (Backend) only calls these methods and never writes raw SQL inside
 * Handlers.
 *
 * TODO(AmirAli): prerequisite - create the users table per the spec (id,
 * username, unique email, hashed password). See/complete schema.sql.
 *
 * Important security note: always use PreparedStatement with '?' , never
 * string concatenation to build a query (to prevent SQL Injection).
 */
public class UserDAO {

    /**
     * TODO(AmirAli): save a new user to the database.
     * Suggested steps:
     *   1. Assume user.getPasswordHash() is already populated using
     *      PasswordUtil.hash(...) (this should happen in AuthHandler before
     *      calling insertUser, or you can design this method to take the raw
     *      password and hash it internally - pick one as a team and stick to it).
     *   2. INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)
     *   3. Get the auto-generated id (Statement.RETURN_GENERATED_KEYS) and set it on User.
     * @return the User with its id populated, or throw SQLException if a
     * UNIQUE constraint is violated (duplicate username/email).
     */
    public User insertUser(User user) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement insertUser in Phase 1");
    }

    /**
     * TODO(AmirAli): find a user by username.
     * SELECT id, username, email, password_hash FROM users WHERE username = ?
     * @return User (including passwordHash so it can be checked in AuthHandler), or null if not found.
     */
    public User getUserByUsername(String username) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getUserByUsername in Phase 1");
    }

    /**
     * TODO(AmirAli - Phase 1 or wherever needed): fetch a user's info by id
     * (e.g. when building Tweet.authorUsername or displaying a profile).
     */
    public User getUserById(int userId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getUserById");
    }

    /** TODO(AmirAli - Phase 4 search): ILIKE %query% on username. See SearchDAO. */

    // Helper example so you don't forget the try-with-resources pattern:
    //
    // try (Connection conn = DatabaseConnection.getInstance().getConnection();
    //      PreparedStatement stmt = conn.prepareStatement("SELECT ... WHERE username = ?")) {
    //     stmt.setString(1, username);
    //     try (ResultSet rs = stmt.executeQuery()) {
    //         if (rs.next()) {
    //             User u = new User();
    //             u.setId(rs.getInt("id"));
    //             u.setUsername(rs.getString("username"));
    //             u.setEmail(rs.getString("email"));
    //             u.setPasswordHash(rs.getString("password_hash"));
    //             return u;
    //         }
    //     }
    // }
    // return null;
}
