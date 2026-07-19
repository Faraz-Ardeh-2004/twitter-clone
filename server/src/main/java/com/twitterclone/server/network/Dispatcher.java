package com.twitterclone.server.network;

import com.twitterclone.server.handler.AuthHandler;
import com.twitterclone.server.handler.SearchHandler;
import com.twitterclone.server.handler.SocialHandler;
import com.twitterclone.server.handler.TweetHandler;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

/**
 * ============================================================
 * Owner: Hesam (Backend) | created in Phase 1, completed across Phases 2/3/4
 * ============================================================
 * The server's central "router": based on packet.getType(), decides which
 * Handler should run the actual logic. ClientHandler should not contain any
 * business logic (DB, business rules) — it should only call this method.
 *
 * TODO(Hesam): in ClientHandler.handleLine, replace the temporary PING block with:
 *   return Dispatcher.dispatch(handler, request);
 *
 * TODO(Hesam): every time you finish a new Handler (e.g. Phase 3:
 * SocialHandler), change the matching case from throwing
 * UnsupportedOperationException to actually calling it.
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

        // TODO(Hesam): for every type that requires being logged in (everything
        // except PING/REGISTER/LOGIN), first check
        // Authenticator.validate(request.getToken()); if it's invalid, return a
        // Packet.error and never reach the Handler.

        switch (type) {
            case PING:
                return Packet.ok(PacketType.PONG, null);

            // ---------- Phase 1: authentication ----------
            case REGISTER:
                return AuthHandler.handleRegister(request);
            case LOGIN:
                return AuthHandler.handleLogin(request, handler);
            case LOGOUT:
                return AuthHandler.handleLogout(request, handler);

            // ---------- Phase 2: tweets and feed ----------
            case CREATE_TWEET:
                return TweetHandler.handleCreateTweet(request);
            case DELETE_TWEET:
                return TweetHandler.handleDeleteTweet(request);
            case GET_TWEET:
                return TweetHandler.handleGetTweet(request);
            case GET_USER_TWEETS:
                return TweetHandler.handleGetUserTweets(request);
            case GET_FEED:
                return TweetHandler.handleGetFeed(request);

            // ---------- Phase 3: social graph and interactions ----------
            case FOLLOW:
                return SocialHandler.handleFollow(request);
            case UNFOLLOW:
                return SocialHandler.handleUnfollow(request);
            case GET_FOLLOWERS:
                return SocialHandler.handleGetFollowers(request);
            case GET_FOLLOWING:
                return SocialHandler.handleGetFollowing(request);
            case LIKE_TWEET:
                return SocialHandler.handleLike(request);
            case UNLIKE_TWEET:
                return SocialHandler.handleUnlike(request);

            // ---------- Phase 4: search ----------
            case SEARCH_USERS:
                return SearchHandler.handleSearchUsers(request);
            case SEARCH_TWEETS:
                return SearchHandler.handleSearchTweets(request);

            default:
                return Packet.error(PacketType.ERROR, "No handler wired for type: " + type);
        }
    }
}
