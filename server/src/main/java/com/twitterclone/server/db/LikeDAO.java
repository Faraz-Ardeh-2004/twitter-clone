package com.twitterclone.server.db;

import java.sql.SQLException;

/**
 * ============================================================
 * Owner: AmirAli (Database) | Phase 3 - Day 9-11
 * ============================================================
 * likes table: composite key (user_id, tweet_id).
 * TODO(AmirAli): create the table:
 *   CREATE TABLE likes (
 *     user_id INT REFERENCES users(id),
 *     tweet_id INT REFERENCES tweets(id),
 *     PRIMARY KEY (user_id, tweet_id)
 *   );
 *
 * WARNING - concurrency note (Phase 4): like/unlike must be idempotent and
 * atomic so the count doesn't break when several clients like the same
 * tweet at the same time. Suggestion: instead of "check then insert" (which
 * has a race condition), use "INSERT ... ON CONFLICT DO NOTHING" (Postgres),
 * or use a transaction with an appropriate isolation level.
 */
public class LikeDAO {

    /**
     * TODO(AmirAli): insert only if not already liked. Per the spec: if the
     * user hasn't liked it yet, insert; if they have, unlike it - you can put
     * this toggle logic here or in SocialHandler (pick one as a team and
     * document it so it's not duplicated).
     */
    public boolean like(int userId, int tweetId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement like in Phase 3 - watch out for race conditions");
    }

    /** TODO(AmirAli): DELETE FROM likes WHERE user_id = ? AND tweet_id = ? */
    public boolean unlike(int userId, int tweetId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement unlike in Phase 3");
    }

    /** TODO(AmirAli): SELECT COUNT(*) FROM likes WHERE tweet_id = ? */
    public int countLikes(int tweetId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement countLikes in Phase 3");
    }

    /** TODO(AmirAli): has userId already liked tweetId? */
    public boolean hasLiked(int userId, int tweetId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement hasLiked in Phase 3");
    }
}
