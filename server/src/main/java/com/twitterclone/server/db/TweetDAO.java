package com.twitterclone.server.db;

import com.twitterclone.shared.model.Tweet;

import java.sql.SQLException;
import java.util.List;

/**
 * ============================================================
 * Owner: AmirAli (Database) | Phase 2 (Day 6-8) base + Phase 3 (Day 9-11) personalized feed
 * ============================================================
 * All SQL access related to the tweets table.
 *
 * TODO(AmirAli - Phase 2): create the tweets table (id, author_id as FK,
 * content, created_at) + an index on created_at DESC (for feed speed).
 * TODO(AmirAli - Phase 3): add a nullable parent_tweet_id column.
 */
public class TweetDAO {

    /**
     * TODO(AmirAli - Phase 2): save the new tweet and set the generated id on it.
     * INSERT INTO tweets (author_id, content, created_at, parent_tweet_id) VALUES (?, ?, now(), ?)
     */
    public Tweet saveTweet(Tweet tweet) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement saveTweet in Phase 2");
    }

    /**
     * TODO(AmirAli - Phase 2): global feed (all tweets, newest first) with pagination.
     * SELECT t.*, u.username FROM tweets t JOIN users u ON t.author_id = u.id
     * ORDER BY t.created_at DESC LIMIT ? OFFSET ?
     * This method is used by GET_FEED in Phase 2, and is replaced by
     * getPersonalizedFeed in Phase 3.
     */
    public List<Tweet> getGlobalTweets(int limit, int offset) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getGlobalTweets in Phase 2");
    }

    /**
     * TODO(AmirAli - Phase 3): the "critical" personalized feed query. Must
     * return tweets from users that userId follows, plus userId's own
     * tweets, ordered by time descending.
     * Suggested query structure (with JOIN or IN):
     *   SELECT t.*, u.username FROM tweets t
     *   JOIN users u ON t.author_id = u.id
     *   WHERE t.author_id = ?
     *      OR t.author_id IN (SELECT following_id FROM follows WHERE follower_id = ?)
     *   ORDER BY t.created_at DESC LIMIT ? OFFSET ?
     */
    public List<Tweet> getPersonalizedFeed(int userId, int limit, int offset) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getPersonalizedFeed in Phase 3");
    }

    /** TODO(AmirAli): all tweets by a specific user (for the profile page). */
    public List<Tweet> getUserTweets(int userId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getUserTweets");
    }

    /** TODO(AmirAli): a specific tweet by id (for the detail/reply page in Phase 3). */
    public Tweet getTweetById(int tweetId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getTweetById");
    }

    /** TODO(AmirAli): all replies to a tweet (parent_tweet_id = tweetId) - Phase 3. */
    public List<Tweet> getReplies(int parentTweetId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement getReplies in Phase 3");
    }

    /** TODO(AmirAli): delete a tweet (only if authorId owns it - Hesam should also repeat this check in the Handler). */
    public boolean deleteTweet(int tweetId, int authorId) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement deleteTweet");
    }
}
