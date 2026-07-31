package com.twitterclone.server.handler;

import com.google.gson.JsonObject;
import com.twitterclone.server.auth.Authenticator;
import com.twitterclone.server.db.PasswordUtil;
import com.twitterclone.server.db.UserDAO;
import com.twitterclone.server.network.ClientHandler;
import com.twitterclone.server.network.ConnectionRegistry;
import com.twitterclone.server.util.Json;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

import java.sql.SQLException;

/**
 * Business logic for REGISTER / LOGIN / LOGOUT. Bridges Dispatcher and
 * UserDAO/Authenticator; contains no raw SQL. The server is the source of
 * truth for validation, independent of any client-side checks.
 */
public class AuthHandler {

    private static final UserDAO userDAO = new UserDAO();

    private AuthHandler() {
    }

    public static Packet handleRegister(Packet request) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return Packet.error(PacketType.REGISTER, "Missing payload");
        }
        String username = trim(Json.getString(payload, "username"));
        String email = trim(Json.getString(payload, "email"));
        String password = Json.getString(payload, "password");

        String validation = validateRegistration(username, email, password);
        if (validation != null) {
            return Packet.error(PacketType.REGISTER, validation);
        }

        try {
            if (userDAO.usernameExists(username)) {
                return Packet.error(PacketType.REGISTER, "Username is already taken");
            }
            if (userDAO.emailExists(email)) {
                return Packet.error(PacketType.REGISTER, "Email is already registered");
            }
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(PasswordUtil.hash(password));
            userDAO.insertUser(user);

            JsonObject out = new JsonObject();
            out.addProperty("userId", user.getId());
            out.addProperty("username", user.getUsername());
            return Packet.ok(PacketType.REGISTER, out);
        } catch (SQLException e) {
            // Unique-constraint race or other DB error.
            if (isUniqueViolation(e)) {
                return Packet.error(PacketType.REGISTER, "Username or email is already in use");
            }
            System.err.println("Register failed: " + e.getMessage());
            return Packet.error(PacketType.REGISTER, "Registration failed, please try again");
        }
    }

    public static Packet handleLogin(Packet request, ClientHandler handler) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return Packet.error(PacketType.LOGIN, "Missing payload");
        }
        String username = trim(Json.getString(payload, "username"));
        String password = Json.getString(payload, "password");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Packet.error(PacketType.LOGIN, "Username and password are required");
        }

        try {
            User user = userDAO.getUserByUsername(username);
            if (user == null || !PasswordUtil.check(password, user.getPasswordHash())) {
                return Packet.error(PacketType.LOGIN, "Invalid username or password");
            }

            String token = Authenticator.createSession(user.getId());
            handler.setUserId(user.getId());
            ConnectionRegistry.register(user.getId(), handler);

            JsonObject out = new JsonObject();
            out.addProperty("userId", user.getId());
            out.addProperty("username", user.getUsername());
            out.addProperty("displayName", user.displayNameOrUsername());
            if (user.getAvatarUrl() != null) {
                out.addProperty("avatarUrl", user.getAvatarUrl());
            }
            Packet resp = Packet.ok(PacketType.LOGIN, out);
            resp.setToken(token);
            return resp;
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
            return Packet.error(PacketType.LOGIN, "Login failed, please try again");
        }
    }

    public static Packet handleLogout(Packet request, ClientHandler handler) {
        Authenticator.invalidate(request.getToken());
        if (handler.getUserId() != null) {
            ConnectionRegistry.unregister(handler.getUserId());
            handler.setUserId(null);
        }
        return Packet.ok(PacketType.LOGOUT, null);
    }

    // --- validation helpers ---

    private static String validateRegistration(String username, String email, String password) {
        if (username == null || username.length() < 3 || username.length() > 50) {
            return "Username must be 3–50 characters";
        }
        if (!username.matches("[A-Za-z0-9_]+")) {
            return "Username may only contain letters, digits, and underscores";
        }
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return "A valid email address is required";
        }
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        return null;
    }

    private static boolean isUniqueViolation(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
