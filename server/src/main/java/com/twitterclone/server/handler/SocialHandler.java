package com.twitterclone.server.handler;

import com.google.gson.JsonObject;
import com.twitterclone.server.db.FollowDAO;
import com.twitterclone.server.db.LikeDAO;
import com.twitterclone.server.db.NotificationDAO;
import com.twitterclone.server.db.TweetDAO;
import com.twitterclone.server.db.UserDAO;
import com.twitterclone.server.network.ConnectionRegistry;
import com.twitterclone.server.util.Json;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

import java.sql.SQLException;
import java.util.List;

/**
 * Follow/unfollow, followers/following lists, like/unlike, and retweets. Each
 * mutating action persists a notification for the affected user and emits the
 * matching real-time push (FOLLOW_PUSH / LIKE_PUSH / NEW_TWEET_PUSH /
 * NOTIFICATION_PUSH) so other connected clients update without a refresh.
 *
 * Expected payloads:
 *   FOLLOW / UNFOLLOW         -> { "targetUserId": int }
 *   GET_FOLLOWERS / FOLLOWING -> { "userId": int }  (defaults to the requester)
 *   LIKE_TWEET / UNLIKE_TWEET -> { "tweetId": int }
 *   RETWEET / UNDO_RETWEET    -> { "tweetId": int }  (the original tweet)
 */
public class SocialHandler {

    private static final FollowDAO followDAO = new FollowDAO();
    private static final LikeDAO likeDAO = new LikeDAO();
    private static final TweetDAO tweetDAO = new TweetDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final NotificationDAO notificationDAO = new NotificationDAO();

    private SocialHandler() {
    }

    public static Packet handleFollow(Packet request, int userId) {
        Integer target = Json.getIntOrNull(request.getPayload(), "targetUserId");
        if (target == null) {
            return Packet.error(PacketType.FOLLOW, "Missing targetUserId");
        }
        if (target == userId) {
            return Packet.error(PacketType.FOLLOW, "You cannot follow yourself");
        }
        try {
            boolean created = followDAO.follow(userId, target);
            if (created) {
                TweetHandler.notifyAndPush(target, userId, "FOLLOW", null);
            }
            int followerCount = followDAO.countFollowers(target);
            pushFollowState(target, userId, true, followerCount);
            return Packet.ok(PacketType.FOLLOW, followResult(target, true, followerCount));
        } catch (SQLException e) {
            System.err.println("follow failed: " + e.getMessage());
            return Packet.error(PacketType.FOLLOW, "Could not follow user");
        }
    }

    public static Packet handleUnfollow(Packet request, int userId) {
        Integer target = Json.getIntOrNull(request.getPayload(), "targetUserId");
        if (target == null) {
            return Packet.error(PacketType.UNFOLLOW, "Missing targetUserId");
        }
        try {
            followDAO.unfollow(userId, target);
            notificationDAO.delete(target, userId, "FOLLOW", null);
            int followerCount = followDAO.countFollowers(target);
            pushFollowState(target, userId, false, followerCount);
            return Packet.ok(PacketType.UNFOLLOW, followResult(target, false, followerCount));
        } catch (SQLException e) {
            System.err.println("unfollow failed: " + e.getMessage());
            return Packet.error(PacketType.UNFOLLOW, "Could not unfollow user");
        }
    }

    public static Packet handleGetFollowers(Packet request, int userId) {
        int target = Json.getInt(request.getPayload(), "userId", userId);
        try {
            List<User> users = userDAO.getFollowers(target, userId);
            return usersResult(PacketType.GET_FOLLOWERS, users);
        } catch (SQLException e) {
            System.err.println("getFollowers failed: " + e.getMessage());
            return Packet.error(PacketType.GET_FOLLOWERS, "Could not load followers");
        }
    }

    public static Packet handleGetFollowing(Packet request, int userId) {
        int target = Json.getInt(request.getPayload(), "userId", userId);
        try {
            List<User> users = userDAO.getFollowing(target, userId);
            return usersResult(PacketType.GET_FOLLOWING, users);
        } catch (SQLException e) {
            System.err.println("getFollowing failed: " + e.getMessage());
            return Packet.error(PacketType.GET_FOLLOWING, "Could not load following");
        }
    }

    public static Packet handleLike(Packet request, int userId) {
        Integer tweetId = Json.getIntOrNull(request.getPayload(), "tweetId");
        if (tweetId == null) {
            return Packet.error(PacketType.LIKE_TWEET, "Missing tweetId");
        }
        try {
            boolean created = likeDAO.like(userId, tweetId);
            if (created) {
                Integer author = tweetDAO.getAuthorId(tweetId);
                if (author != null) {
                    TweetHandler.notifyAndPush(author, userId, "LIKE", tweetId);
                }
            }
            int likeCount = likeDAO.countLikes(tweetId);
            pushLikeState(tweetId, likeCount);
            return Packet.ok(PacketType.LIKE_TWEET, likeResult(tweetId, true, likeCount));
        } catch (SQLException e) {
            System.err.println("like failed: " + e.getMessage());
            return Packet.error(PacketType.LIKE_TWEET, "Could not like tweet");
        }
    }

    public static Packet handleUnlike(Packet request, int userId) {
        Integer tweetId = Json.getIntOrNull(request.getPayload(), "tweetId");
        if (tweetId == null) {
            return Packet.error(PacketType.UNLIKE_TWEET, "Missing tweetId");
        }
        try {
            likeDAO.unlike(userId, tweetId);
            Integer author = tweetDAO.getAuthorId(tweetId);
            if (author != null) {
                notificationDAO.delete(author, userId, "LIKE", tweetId);
            }
            int likeCount = likeDAO.countLikes(tweetId);
            pushLikeState(tweetId, likeCount);
            return Packet.ok(PacketType.UNLIKE_TWEET, likeResult(tweetId, false, likeCount));
        } catch (SQLException e) {
            System.err.println("unlike failed: " + e.getMessage());
            return Packet.error(PacketType.UNLIKE_TWEET, "Could not unlike tweet");
        }
    }

    public static Packet handleRetweet(Packet request, int userId) {
        Integer tweetId = Json.getIntOrNull(request.getPayload(), "tweetId");
        if (tweetId == null) {
            return Packet.error(PacketType.RETWEET, "Missing tweetId");
        }
        try {
            Integer originalAuthor = tweetDAO.retweet(userId, tweetId);
            Tweet display = tweetDAO.getTweetById(tweetId, userId);
            if (display == null) {
                return Packet.error(PacketType.RETWEET, "Tweet not found");
            }
            if (originalAuthor != null) {
                // A new repost was created: notify the author and surface the
                // repost in the retweeter's followers' feeds.
                TweetHandler.notifyAndPush(originalAuthor, userId, "RETWEET", tweetId);
                User me = userDAO.getUserById(userId);
                display.setRetweetOf(tweetId);
                display.setRetweetedBy(me != null ? me.getUsername() : null);
                ConnectionRegistry.broadcastToFollowers(userId,
                        Packet.push(PacketType.NEW_TWEET_PUSH, Json.tree(display).getAsJsonObject()));
            }
            return Packet.ok(PacketType.RETWEET, retweetResult(tweetId, true, display.getRetweetCount()));
        } catch (SQLException e) {
            System.err.println("retweet failed: " + e.getMessage());
            return Packet.error(PacketType.RETWEET, "Could not retweet");
        }
    }

    public static Packet handleUndoRetweet(Packet request, int userId) {
        Integer tweetId = Json.getIntOrNull(request.getPayload(), "tweetId");
        if (tweetId == null) {
            return Packet.error(PacketType.UNDO_RETWEET, "Missing tweetId");
        }
        try {
            tweetDAO.undoRetweet(userId, tweetId);
            Integer author = tweetDAO.getAuthorId(tweetId);
            if (author != null) {
                notificationDAO.delete(author, userId, "RETWEET", tweetId);
            }
            Tweet t = tweetDAO.getTweetById(tweetId, userId);
            int count = t == null ? 0 : t.getRetweetCount();
            return Packet.ok(PacketType.UNDO_RETWEET, retweetResult(tweetId, false, count));
        } catch (SQLException e) {
            System.err.println("undoRetweet failed: " + e.getMessage());
            return Packet.error(PacketType.UNDO_RETWEET, "Could not undo retweet");
        }
    }

    // --- push helpers ---

    private static void pushFollowState(int targetUserId, int followerId, boolean following, int followerCount) {
        JsonObject p = new JsonObject();
        p.addProperty("targetUserId", targetUserId);
        p.addProperty("followerId", followerId);
        p.addProperty("following", following);
        p.addProperty("followerCount", followerCount);
        ConnectionRegistry.sendTo(targetUserId, Packet.push(PacketType.FOLLOW_PUSH, p));
    }

    private static void pushLikeState(int tweetId, int likeCount) {
        JsonObject p = new JsonObject();
        p.addProperty("tweetId", tweetId);
        p.addProperty("likeCount", likeCount);
        // Any client showing this tweet should update its like count.
        ConnectionRegistry.broadcastToAll(Packet.push(PacketType.LIKE_PUSH, p));
    }

    // --- response builders ---

    private static JsonObject followResult(int targetUserId, boolean following, int followerCount) {
        JsonObject p = new JsonObject();
        p.addProperty("targetUserId", targetUserId);
        p.addProperty("following", following);
        p.addProperty("followerCount", followerCount);
        return p;
    }

    private static JsonObject likeResult(int tweetId, boolean liked, int likeCount) {
        JsonObject p = new JsonObject();
        p.addProperty("tweetId", tweetId);
        p.addProperty("liked", liked);
        p.addProperty("likeCount", likeCount);
        return p;
    }

    private static JsonObject retweetResult(int tweetId, boolean retweeted, int retweetCount) {
        JsonObject p = new JsonObject();
        p.addProperty("tweetId", tweetId);
        p.addProperty("retweeted", retweeted);
        p.addProperty("retweetCount", retweetCount);
        return p;
    }

    private static Packet usersResult(PacketType type, List<User> users) {
        JsonObject out = new JsonObject();
        out.add("users", Json.array(users));
        return Packet.ok(type, out);
    }
}
