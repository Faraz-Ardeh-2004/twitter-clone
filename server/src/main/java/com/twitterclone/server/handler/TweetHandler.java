package com.twitterclone.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.server.auth.Authenticator;
import com.twitterclone.server.db.TweetDAO;
import com.twitterclone.server.network.ConnectionRegistry;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 2 (Day 6-8) base + Phase 3 (Day 9-11) personalized feed
 * ============================================================
 * Tweet business logic. The most important part: after saving a new tweet,
 * "push" it to online clients (NEW_TWEET_PUSH) - this is what creates the
 * real-time experience.
 *
 * Expected payload shapes (coordinate with Faraz):
 *   CREATE_TWEET -> { "content": "...", "parentTweetId": null or a number }
 *   DELETE_TWEET -> { "tweetId": 123 }
 *   GET_FEED     -> { "limit": 20, "offset": 0 }
 */
public class TweetHandler {

    private static final TweetDAO tweetDAO = new TweetDAO();
    private static final Gson gson = new Gson();

    private TweetHandler() {
    }

    /**
     * TODO(Hesam - Phase 2):
     *  1. Validate the token with Authenticator.validate(request.getToken()) -> userId.
     *  2. Read content from the payload (and, per the bonus rules, enforce a
     *     length limit if you implement it).
     *  3. Build a new Tweet (authorId=userId, content, createdAt=now) and call tweetDAO.saveTweet(tweet).
     *  4. Build a NEW_TWEET_PUSH packet with payload = the new tweet (serialized with Gson).
     *  5. TODO(Phase 2, simple): ConnectionRegistry.broadcastToAll(pushPacket)
     *     TODO(Phase 3, better): ConnectionRegistry.broadcastToFollowers(userId, pushPacket)
     *  6. Return Packet.ok(PacketType.CREATE_TWEET, payload) as the response
     *     to the original request.
     */
    public static Packet handleCreateTweet(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleCreateTweet in Phase 2");
    }

    /**
     * TODO(Hesam - Phase 2): read tweetId, call tweetDAO.deleteTweet(tweetId, userId)
     * (only the owner may delete - put this check here or in the DAO).
     */
    public static Packet handleDeleteTweet(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleDeleteTweet in Phase 2");
    }

    /** TODO(Hesam - Phase 3): a specific tweet with details (for the reply page). */
    public static Packet handleGetTweet(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleGetTweet in Phase 3");
    }

    /** TODO(Hesam - Phase 2): all tweets by a specific user (profile page). */
    public static Packet handleGetUserTweets(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleGetUserTweets in Phase 2");
    }

    /**
     * TODO(Hesam):
     *  Phase 2: use tweetDAO.getGlobalTweets(limit, offset) (simple global feed).
     *  Phase 3: change this method to use
     *  tweetDAO.getPersonalizedFeed(userId, limit, offset) instead (per the
     *  Phase 3 DoD: personalized feed).
     */
    public static Packet handleGetFeed(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleGetFeed in Phase 2 (update in Phase 3)");
    }
}
