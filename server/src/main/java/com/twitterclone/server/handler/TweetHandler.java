package com.twitterclone.server.handler;

import com.google.gson.JsonObject;
import com.twitterclone.server.db.NotificationDAO;
import com.twitterclone.server.db.TweetDAO;
import com.twitterclone.server.network.ConnectionRegistry;
import com.twitterclone.server.util.Json;
import com.twitterclone.shared.model.Notification;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import com.twitterclone.shared.protocol.Protocol;

import java.sql.SQLException;
import java.util.List;

/**
 * Tweet business logic. The key real-time moment: after saving a top-level
 * tweet, it is pushed (NEW_TWEET_PUSH) to the author and their online
 * followers. Replies instead notify the parent author (NOTIFICATION_PUSH).
 *
 * Expected payloads:
 *   CREATE_TWEET   -> { "content": "...", "parentTweetId": null|int, "media": ["data:...", ...] }
 *   DELETE_TWEET   -> { "tweetId": int }
 *   GET_TWEET      -> { "tweetId": int }
 *   GET_USER_TWEETS-> { "userId": int }   (defaults to the requester)
 *   GET_FEED       -> { "limit": int, "offset": int, "global": bool }
 */
public class TweetHandler {

    private static final TweetDAO tweetDAO = new TweetDAO();
    private static final NotificationDAO notificationDAO = new NotificationDAO();

    private TweetHandler() {
    }

    public static Packet handleCreateTweet(Packet request, int userId) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return Packet.error(PacketType.CREATE_TWEET, "Missing payload");
        }
        String content = Json.getString(payload, "content");
        content = content == null ? "" : content.trim();
        Integer parentId = Json.getIntOrNull(payload, "parentTweetId");
        List<String> media = Json.getStringList(payload, "media");

        if (content.isEmpty() && media.isEmpty()) {
            return Packet.error(PacketType.CREATE_TWEET, "A tweet needs text or an image");
        }
        if (content.length() > Protocol.MAX_TWEET_LENGTH) {
            return Packet.error(PacketType.CREATE_TWEET,
                    "Tweet exceeds the " + Protocol.MAX_TWEET_LENGTH + " character limit");
        }
        if (media.size() > Protocol.MAX_MEDIA_PER_TWEET) {
            return Packet.error(PacketType.CREATE_TWEET,
                    "At most " + Protocol.MAX_MEDIA_PER_TWEET + " images per tweet");
        }
        for (String m : media) {
            if (m.length() > Protocol.MAX_MEDIA_BASE64_LENGTH) {
                return Packet.error(PacketType.CREATE_TWEET, "One of the images is too large");
            }
        }

        try {
            Tweet tweet = tweetDAO.createTweet(userId, content, parentId, media);

            if (parentId == null) {
                // Top-level tweet: push into followers' feeds in real time.
                JsonObject pushPayload = Json.tree(tweet).getAsJsonObject();
                ConnectionRegistry.broadcastToFollowers(userId, Packet.push(PacketType.NEW_TWEET_PUSH, pushPayload));
            } else {
                // Reply: notify the author of the parent tweet.
                Integer parentAuthor = tweetDAO.getAuthorId(parentId);
                if (parentAuthor != null) {
                    notifyAndPush(parentAuthor, userId, "REPLY", parentId);
                }
            }

            JsonObject out = new JsonObject();
            out.add("tweet", Json.tree(tweet));
            return Packet.ok(PacketType.CREATE_TWEET, out);
        } catch (SQLException e) {
            System.err.println("createTweet failed: " + e.getMessage());
            return Packet.error(PacketType.CREATE_TWEET, "Could not publish tweet");
        }
    }

    public static Packet handleDeleteTweet(Packet request, int userId) {
        Integer tweetId = Json.getIntOrNull(request.getPayload(), "tweetId");
        if (tweetId == null) {
            return Packet.error(PacketType.DELETE_TWEET, "Missing tweetId");
        }
        try {
            boolean deleted = tweetDAO.deleteTweet(tweetId, userId);
            if (!deleted) {
                return Packet.error(PacketType.DELETE_TWEET, "Tweet not found or not yours to delete");
            }
            JsonObject out = new JsonObject();
            out.addProperty("tweetId", tweetId);
            return Packet.ok(PacketType.DELETE_TWEET, out);
        } catch (SQLException e) {
            System.err.println("deleteTweet failed: " + e.getMessage());
            return Packet.error(PacketType.DELETE_TWEET, "Could not delete tweet");
        }
    }

    public static Packet handleGetTweet(Packet request, int userId) {
        Integer tweetId = Json.getIntOrNull(request.getPayload(), "tweetId");
        if (tweetId == null) {
            return Packet.error(PacketType.GET_TWEET, "Missing tweetId");
        }
        try {
            Tweet tweet = tweetDAO.getTweetById(tweetId, userId);
            if (tweet == null) {
                return Packet.error(PacketType.GET_TWEET, "Tweet not found");
            }
            List<Tweet> replies = tweetDAO.getReplies(tweetId, userId);
            JsonObject out = new JsonObject();
            out.add("tweet", Json.tree(tweet));
            out.add("replies", Json.array(replies));
            return Packet.ok(PacketType.GET_TWEET, out);
        } catch (SQLException e) {
            System.err.println("getTweet failed: " + e.getMessage());
            return Packet.error(PacketType.GET_TWEET, "Could not load tweet");
        }
    }

    public static Packet handleGetUserTweets(Packet request, int userId) {
        int target = Json.getInt(request.getPayload(), "userId", userId);
        try {
            List<Tweet> tweets = tweetDAO.getUserTweets(target, userId);
            JsonObject out = new JsonObject();
            out.add("tweets", Json.array(tweets));
            return Packet.ok(PacketType.GET_USER_TWEETS, out);
        } catch (SQLException e) {
            System.err.println("getUserTweets failed: " + e.getMessage());
            return Packet.error(PacketType.GET_USER_TWEETS, "Could not load tweets");
        }
    }

    public static Packet handleGetFeed(Packet request, int userId) {
        JsonObject payload = request.getPayload();
        int limit = clamp(Json.getInt(payload, "limit", 30), 1, 100);
        int offset = Math.max(0, Json.getInt(payload, "offset", 0));
        boolean global = Json.getBool(payload, "global", false);
        try {
            List<Tweet> tweets = global
                    ? tweetDAO.getGlobalFeed(userId, limit, offset)
                    : tweetDAO.getPersonalizedFeed(userId, limit, offset);
            JsonObject out = new JsonObject();
            out.add("tweets", Json.array(tweets));
            return Packet.ok(PacketType.GET_FEED, out);
        } catch (SQLException e) {
            System.err.println("getFeed failed: " + e.getMessage());
            return Packet.error(PacketType.GET_FEED, "Could not load feed");
        }
    }

    /** Persists a notification and pushes it to the recipient if they are online. */
    static void notifyAndPush(int recipientId, int actorId, String type, Integer tweetId) throws SQLException {
        Notification n = notificationDAO.create(recipientId, actorId, type, tweetId);
        if (n != null) {
            ConnectionRegistry.sendTo(recipientId,
                    Packet.push(PacketType.NOTIFICATION_PUSH, Json.tree(n).getAsJsonObject()));
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
