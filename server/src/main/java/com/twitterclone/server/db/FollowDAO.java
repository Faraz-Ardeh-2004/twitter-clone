package com.twitterclone.server.db;

import java.sql.SQLException;
import java.util.List;

/**
 * ============================================================
 * Owner: AmirAli (Database) | Phase 3 - Day 9-11
 * ============================================================
 * follows table: composite key (follower_id, following_id).
 * TODO(AmirAli): create the table:
 *   CREATE TABLE follows (
 *     follower_id INT REFERENCES users(id),
 *     following_id INT REFERENCES users(id),
 *     PRIMARY KEY (follower_id, following_id)
 *   );
 */
public class FollowDAO {

    /** TODO(AmirAli): INSERT INTO follows ...; if the relationship already exists, ignore it (idempotent). */
    public boolean follow(int followerId, int followingId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement follow in Phase 3");
    }

    /** TODO(AmirAli): DELETE FROM follows WHERE follower_id = ? AND following_id = ? */
    public boolean unfollow(int followerId, int followingId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement unfollow in Phase 3");
    }

    /** TODO(AmirAli): list of ids of everyone who follows userId. */
    public List<Integer> getFollowerIds(int userId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getFollowerIds in Phase 3");
    }

    /** TODO(AmirAli): list of ids of everyone userId follows. */
    public List<Integer> getFollowingIds(int userId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getFollowingIds in Phase 3");
    }

    /** TODO(AmirAli): does followerId currently follow followingId? (SELECT 1 ... LIMIT 1) */
    public boolean isFollowing(int followerId, int followingId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement isFollowing in Phase 3");
    }

    /** TODO(AmirAli): follower/following counts for the profile page - simple COUNT(*). */
    public int countFollowers(int userId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement countFollowers in Phase 3");
    }

    public int countFollowing(int userId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement countFollowing in Phase 3");
    }
}
