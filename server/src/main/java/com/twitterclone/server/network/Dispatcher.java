package com.twitterclone.server.network;

import com.twitterclone.server.auth.Authenticator;
import com.twitterclone.server.handler.AuthHandler;
import com.twitterclone.server.handler.NotificationHandler;
import com.twitterclone.server.handler.ProfileHandler;
import com.twitterclone.server.handler.SearchHandler;
import com.twitterclone.server.handler.SocialHandler;
import com.twitterclone.server.handler.TweetHandler;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

/**
 * The server's central router. Based on packet type it authenticates the
 * request (all types except PING/REGISTER/LOGIN require a valid session token)
 * and delegates to the matching Handler with the resolved userId. ClientHandler
 * holds no business logic — it only calls {@link #dispatch}.
 */
public class Dispatcher {

    private Dispatcher() {
    }

    public static Packet dispatch(ClientHandler handler, Packet request) {
        PacketType type;
        try {
            type = PacketType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            return Packet.error(PacketType.ERROR, "Unknown packet type: " + request.getType());
        }

        // Public endpoints require no session.
        switch (type) {
            case PING:
                return Packet.ok(PacketType.PONG, null);
            case REGISTER:
                return AuthHandler.handleRegister(request);
            case LOGIN:
                return AuthHandler.handleLogin(request, handler);
            default:
                break;
        }

        // Everything below requires a valid session token.
        Integer userId = Authenticator.validate(request.getToken());
        if (userId == null) {
            return Packet.error(type, "Not authenticated — please log in again");
        }

        switch (type) {
            case LOGOUT:
                return AuthHandler.handleLogout(request, handler);

            // ---------- tweets & feed ----------
            case CREATE_TWEET:
                return TweetHandler.handleCreateTweet(request, userId);
            case DELETE_TWEET:
                return TweetHandler.handleDeleteTweet(request, userId);
            case GET_TWEET:
                return TweetHandler.handleGetTweet(request, userId);
            case GET_USER_TWEETS:
                return TweetHandler.handleGetUserTweets(request, userId);
            case GET_FEED:
                return TweetHandler.handleGetFeed(request, userId);

            // ---------- social graph & interactions ----------
            case FOLLOW:
                return SocialHandler.handleFollow(request, userId);
            case UNFOLLOW:
                return SocialHandler.handleUnfollow(request, userId);
            case GET_FOLLOWERS:
                return SocialHandler.handleGetFollowers(request, userId);
            case GET_FOLLOWING:
                return SocialHandler.handleGetFollowing(request, userId);
            case LIKE_TWEET:
                return SocialHandler.handleLike(request, userId);
            case UNLIKE_TWEET:
                return SocialHandler.handleUnlike(request, userId);
            case RETWEET:
                return SocialHandler.handleRetweet(request, userId);
            case UNDO_RETWEET:
                return SocialHandler.handleUndoRetweet(request, userId);

            // ---------- profiles ----------
            case GET_PROFILE:
                return ProfileHandler.handleGetProfile(request, userId);
            case UPDATE_PROFILE:
                return ProfileHandler.handleUpdateProfile(request, userId);

            // ---------- search ----------
            case SEARCH_USERS:
                return SearchHandler.handleSearchUsers(request, userId);
            case SEARCH_TWEETS:
                return SearchHandler.handleSearchTweets(request, userId);
            case SEARCH_HASHTAG:
                return SearchHandler.handleSearchHashtag(request, userId);
            case TRENDING_HASHTAGS:
                return SearchHandler.handleTrending(request, userId);

            // ---------- notifications ----------
            case GET_NOTIFICATIONS:
                return NotificationHandler.handleGet(request, userId);
            case MARK_NOTIFICATIONS_READ:
                return NotificationHandler.handleMarkRead(request, userId);

            default:
                return Packet.error(type, "No handler wired for type: " + type);
        }
    }
}
