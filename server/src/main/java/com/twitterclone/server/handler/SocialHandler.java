package com.twitterclone.server.handler;

import com.twitterclone.server.db.FollowDAO;
import com.twitterclone.server.db.LikeDAO;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 3 - Day 9-11
 * ============================================================
 * Business logic for follow/unfollow and like/unlike.
 *
 * Expected payload shapes (coordinate with Faraz):
 *   FOLLOW / UNFOLLOW -> { "targetUserId": 42 }
 *   LIKE_TWEET / UNLIKE_TWEET -> { "tweetId": 123 }
 */
public class SocialHandler {

    private static final FollowDAO followDAO = new FollowDAO();
    private static final LikeDAO likeDAO = new LikeDAO();

    private SocialHandler() {
    }

    /** TODO(Hesam): token -> userId, targetUserId from payload, followDAO.follow(userId, targetUserId). */
    public static Packet handleFollow(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleFollow in Phase 3");
    }

    /** TODO(Hesam): same as handleFollow but calls followDAO.unfollow(...). */
    public static Packet handleUnfollow(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleUnfollow in Phase 3");
    }

    /** TODO(Hesam): list of followers for a user (userId from the payload or from an opened profile). */
    public static Packet handleGetFollowers(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleGetFollowers in Phase 3");
    }

    /** TODO(Hesam): list of users a given user follows. */
    public static Packet handleGetFollowing(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleGetFollowing in Phase 3");
    }

    /**
     * TODO(Hesam): per the spec - if not already liked, register the like; if
     * already liked, unlike it (toggle). Before implementing, coordinate with
     * AmirAli on where this toggle logic lives (here or inside LikeDAO) so
     * it's not duplicated.
     */
    public static Packet handleLike(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleLike in Phase 3");
    }

    /** TODO(Hesam): likeDAO.unlike(userId, tweetId). */
    public static Packet handleUnlike(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleUnlike in Phase 3");
    }
}
