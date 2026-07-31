package com.twitterclone.server.handler;

import com.google.gson.JsonObject;
import com.twitterclone.server.db.UserDAO;
import com.twitterclone.server.util.Json;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

import java.sql.SQLException;

/**
 * Profile viewing and editing.
 * Expected payloads:
 *   GET_PROFILE    -> { "userId": int } or { "username": "..." } (defaults to self)
 *   UPDATE_PROFILE -> { "displayName": "...", "bio": "...", "avatarUrl": "...", "bannerUrl": "..." }
 *                     (any subset; only provided fields are changed)
 */
public class ProfileHandler {

    private static final UserDAO userDAO = new UserDAO();

    private ProfileHandler() {
    }

    public static Packet handleGetProfile(Packet request, int userId) {
        JsonObject payload = request.getPayload();
        try {
            Integer targetId = Json.getIntOrNull(payload, "userId");
            String username = Json.getString(payload, "username");

            if (targetId == null && username != null) {
                User byName = userDAO.getUserByUsername(username);
                if (byName == null) {
                    return Packet.error(PacketType.GET_PROFILE, "User not found");
                }
                targetId = byName.getId();
            }
            if (targetId == null) {
                targetId = userId; // own profile
            }

            User profile = userDAO.getProfile(targetId, userId);
            if (profile == null) {
                return Packet.error(PacketType.GET_PROFILE, "User not found");
            }
            JsonObject out = new JsonObject();
            out.add("user", Json.tree(profile));
            out.addProperty("self", targetId == userId);
            return Packet.ok(PacketType.GET_PROFILE, out);
        } catch (SQLException e) {
            System.err.println("getProfile failed: " + e.getMessage());
            return Packet.error(PacketType.GET_PROFILE, "Could not load profile");
        }
    }

    public static Packet handleUpdateProfile(Packet request, int userId) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return Packet.error(PacketType.UPDATE_PROFILE, "Missing payload");
        }
        String displayName = Json.getString(payload, "displayName");
        String bio = Json.getString(payload, "bio");
        String avatarUrl = Json.getString(payload, "avatarUrl");
        String bannerUrl = Json.getString(payload, "bannerUrl");

        if (displayName != null && displayName.length() > 100) {
            return Packet.error(PacketType.UPDATE_PROFILE, "Display name is too long (max 100)");
        }
        if (bio != null && bio.length() > 280) {
            return Packet.error(PacketType.UPDATE_PROFILE, "Bio is too long (max 280)");
        }
        try {
            userDAO.updateProfile(userId, displayName, bio, avatarUrl, bannerUrl);
            User updated = userDAO.getProfile(userId, userId);
            JsonObject out = new JsonObject();
            out.add("user", Json.tree(updated));
            return Packet.ok(PacketType.UPDATE_PROFILE, out);
        } catch (SQLException e) {
            System.err.println("updateProfile failed: " + e.getMessage());
            return Packet.error(PacketType.UPDATE_PROFILE, "Could not update profile");
        }
    }
}
