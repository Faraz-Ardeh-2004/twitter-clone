package com.twitterclone.server.handler;

import com.google.gson.JsonObject;
import com.twitterclone.server.db.NotificationDAO;
import com.twitterclone.server.util.Json;
import com.twitterclone.shared.model.Notification;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

import java.sql.SQLException;
import java.util.List;

/**
 * Notification listing and read-state management.
 * Expected payloads:
 *   GET_NOTIFICATIONS        -> { "limit": int }  (optional)
 *   MARK_NOTIFICATIONS_READ  -> {}
 */
public class NotificationHandler {

    private static final NotificationDAO notificationDAO = new NotificationDAO();

    private NotificationHandler() {
    }

    public static Packet handleGet(Packet request, int userId) {
        int limit = Json.getInt(request.getPayload(), "limit", 50);
        try {
            List<Notification> notifications = notificationDAO.list(userId, limit);
            int unread = notificationDAO.unreadCount(userId);
            JsonObject out = new JsonObject();
            out.add("notifications", Json.array(notifications));
            out.addProperty("unread", unread);
            return Packet.ok(PacketType.GET_NOTIFICATIONS, out);
        } catch (SQLException e) {
            System.err.println("getNotifications failed: " + e.getMessage());
            return Packet.error(PacketType.GET_NOTIFICATIONS, "Could not load notifications");
        }
    }

    public static Packet handleMarkRead(Packet request, int userId) {
        try {
            notificationDAO.markAllRead(userId);
            return Packet.ok(PacketType.MARK_NOTIFICATIONS_READ, null);
        } catch (SQLException e) {
            System.err.println("markRead failed: " + e.getMessage());
            return Packet.error(PacketType.MARK_NOTIFICATIONS_READ, "Could not update notifications");
        }
    }
}
