package com.twitterclone.server.db;

import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.util.HashtagParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * All SQL access for the {@code tweets} table (and the media/hashtag data
 * attached to tweets). Reads are "viewer-aware": every tweet is annotated with
 * whether the requesting user liked/retweeted it, plus like/reply/retweet
 * counts. Feed reads also flatten retweets so a repost renders as the original
 * tweet with a "retweeted by X" marker.
 */
public class TweetDAO {

    private final MediaDAO mediaDAO = new MediaDAO();
    private final HashtagDAO hashtagDAO = new HashtagDAO();

    // Column list shared by every read. ':order' rows are aliased consistently
    // so a single mapRow() can handle both the flattened feed shape and the
    // plain single-tweet shape.
    private static final String COUNTS =
            "  (SELECT COUNT(*) FROM likes l WHERE l.tweet_id = d.id) AS like_count, " +
            "  (SELECT COUNT(*) FROM tweets rp WHERE rp.parent_tweet_id = d.id) AS reply_count, " +
            "  (SELECT COUNT(*) FROM tweets rt WHERE rt.retweet_of = d.id) AS retweet_count, " +
            "  EXISTS(SELECT 1 FROM likes l WHERE l.tweet_id = d.id AND l.user_id = ?) AS liked, " +
            "  EXISTS(SELECT 1 FROM tweets rt WHERE rt.retweet_of = d.id AND rt.author_id = ?) AS retweeted ";

    // Feed shape: r = the feed row (may be a repost), d = the display tweet
    // (original when r is a repost, else r itself).
    private static final String FEED_SELECT =
            "SELECT d.id, d.author_id, d.content, d.created_at, d.parent_tweet_id, " +
            "  au.username AS author_username, au.display_name AS author_display_name, au.avatar_url AS author_avatar, " +
            "  r.retweet_of AS retweet_marker, ru.username AS retweeted_by, " +
            COUNTS +
            "FROM tweets r " +
            "JOIN tweets d ON d.id = COALESCE(r.retweet_of, r.id) " +
            "JOIN users au ON d.author_id = au.id " +
            "LEFT JOIN users ru ON r.author_id = ru.id ";

    // Single shape: d = t directly, no retweet flattening.
    private static final String SINGLE_SELECT =
            "SELECT d.id, d.author_id, d.content, d.created_at, d.parent_tweet_id, " +
            "  au.username AS author_username, au.display_name AS author_display_name, au.avatar_url AS author_avatar, " +
            "  NULL::int AS retweet_marker, NULL::varchar AS retweeted_by, " +
            COUNTS +
            "FROM tweets d JOIN users au ON d.author_id = au.id ";

    // ---------------------------------------------------------------- writes

    /**
     * Creates a tweet (or reply) together with its media and parsed hashtags in
     * a single transaction, then returns the fully-populated Tweet as the
     * author would see it.
     */
    public Tweet createTweet(int authorId, String content, Integer parentId, List<String> media) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int tweetId;
                String sql = "INSERT INTO tweets (author_id, content, parent_tweet_id) VALUES (?, ?, ?) " +
                        "RETURNING id";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, authorId);
                    stmt.setString(2, content);
                    if (parentId != null) {
                        stmt.setInt(3, parentId);
                    } else {
                        stmt.setNull(3, Types.INTEGER);
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        rs.next();
                        tweetId = rs.getInt(1);
                    }
                }
                mediaDAO.insert(conn, tweetId, media);
                hashtagDAO.attach(conn, tweetId, HashtagParser.extract(content));
                conn.commit();
                return getTweetById(tweetId, authorId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Reposts {@code originalId} on behalf of {@code userId}. No-op if the user
     * already retweeted it. Returns the original tweet's author id when a new
     * repost was created (so the caller can notify them), or null otherwise.
     */
    public Integer retweet(int userId, int originalId) throws SQLException {
        String sql = "INSERT INTO tweets (author_id, content, retweet_of) " +
                "SELECT ?, '', ? WHERE EXISTS (SELECT 1 FROM tweets WHERE id = ?) " +
                "  AND NOT EXISTS (SELECT 1 FROM tweets WHERE author_id = ? AND retweet_of = ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, originalId);
            stmt.setInt(3, originalId);
            stmt.setInt(4, userId);
            stmt.setInt(5, originalId);
            int rows = stmt.executeUpdate();
            return rows > 0 ? getAuthorId(originalId) : null;
        }
    }

    public boolean undoRetweet(int userId, int originalId) throws SQLException {
        String sql = "DELETE FROM tweets WHERE author_id = ? AND retweet_of = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, originalId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a tweet (only if {@code authorId} owns it) together with its whole
     * reply subtree and their likes, in one transaction. media, hashtag links,
     * notifications, and retweets referencing these tweets are removed by their
     * ON DELETE CASCADE constraints; likes on the pre-existing table are deleted
     * explicitly. Returns false if the tweet does not exist or is not owned by
     * the caller.
     */
    public boolean deleteTweet(int tweetId, int authorId) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Ownership check on the root tweet.
                try (PreparedStatement owner = conn.prepareStatement(
                        "SELECT author_id FROM tweets WHERE id = ?")) {
                    owner.setInt(1, tweetId);
                    try (ResultSet rs = owner.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) != authorId) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // Collect the tweet and every reply beneath it (recursively).
                List<Integer> ids = new ArrayList<>();
                String recursive =
                        "WITH RECURSIVE thread AS (" +
                        "  SELECT id FROM tweets WHERE id = ? " +
                        "  UNION ALL " +
                        "  SELECT t.id FROM tweets t JOIN thread th ON t.parent_tweet_id = th.id" +
                        ") SELECT id FROM thread";
                try (PreparedStatement ps = conn.prepareStatement(recursive)) {
                    ps.setInt(1, tweetId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ids.add(rs.getInt(1));
                        }
                    }
                }

                java.sql.Array idArray = conn.createArrayOf("integer", ids.toArray());
                try (PreparedStatement delLikes = conn.prepareStatement(
                        "DELETE FROM likes WHERE tweet_id = ANY(?)")) {
                    delLikes.setArray(1, idArray);
                    delLikes.executeUpdate();
                }
                // FK checks for parent_tweet_id run at statement end, so deleting
                // parents and children in a single statement is safe.
                try (PreparedStatement delTweets = conn.prepareStatement(
                        "DELETE FROM tweets WHERE id = ANY(?)")) {
                    delTweets.setArray(1, idArray);
                    delTweets.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ---------------------------------------------------------------- reads

    /** Global feed (all top-level tweets + retweets), newest first. */
    public List<Tweet> getGlobalFeed(int viewerId, int limit, int offset) throws SQLException {
        String sql = FEED_SELECT +
                "WHERE r.parent_tweet_id IS NULL ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        return query(sql, viewerId, ps -> {
            ps.setInt(3, limit);
            ps.setInt(4, offset);
        });
    }

    /** Personalized feed: the viewer's own posts + posts/retweets from people they follow. */
    public List<Tweet> getPersonalizedFeed(int userId, int limit, int offset) throws SQLException {
        String sql = FEED_SELECT +
                "WHERE r.parent_tweet_id IS NULL " +
                "  AND (r.author_id = ? OR r.author_id IN " +
                "       (SELECT following_id FROM follows WHERE follower_id = ?)) " +
                "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        return query(sql, userId, ps -> {
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, limit);
            ps.setInt(6, offset);
        });
    }

    /** A user's own tweets and retweets (their profile timeline), newest first. */
    public List<Tweet> getUserTweets(int profileUserId, int viewerId) throws SQLException {
        String sql = FEED_SELECT +
                "WHERE r.parent_tweet_id IS NULL AND r.author_id = ? ORDER BY r.created_at DESC";
        return query(sql, viewerId, ps -> ps.setInt(3, profileUserId));
    }

    /** A single tweet by id (as itself, not flattened), for the detail page. */
    public Tweet getTweetById(int tweetId, int viewerId) throws SQLException {
        String sql = SINGLE_SELECT + "WHERE d.id = ?";
        List<Tweet> list = query(sql, viewerId, ps -> ps.setInt(3, tweetId));
        return list.isEmpty() ? null : list.get(0);
    }

    /** Replies to a tweet, oldest first (thread reads top to bottom). */
    public List<Tweet> getReplies(int parentTweetId, int viewerId) throws SQLException {
        String sql = SINGLE_SELECT + "WHERE d.parent_tweet_id = ? ORDER BY d.created_at ASC";
        return query(sql, viewerId, ps -> ps.setInt(3, parentTweetId));
    }

    /** Tweets whose content matches the query, newest first. */
    public List<Tweet> searchTweets(String queryText, int viewerId) throws SQLException {
        String sql = SINGLE_SELECT + "WHERE d.content ILIKE ? ORDER BY d.created_at DESC LIMIT 100";
        return query(sql, viewerId, ps -> ps.setString(3, "%" + queryText + "%"));
    }

    /** Tweets carrying a given hashtag (tag without '#', case-insensitive), newest first. */
    public List<Tweet> getTweetsByHashtag(String tag, int viewerId) throws SQLException {
        String sql = SINGLE_SELECT +
                "JOIN tweet_hashtags th ON th.tweet_id = d.id " +
                "JOIN hashtags h ON h.id = th.hashtag_id " +
                "WHERE h.tag = ? ORDER BY d.created_at DESC LIMIT 100";
        return query(sql, viewerId, ps -> ps.setString(3, tag.toLowerCase()));
    }

    public Integer getAuthorId(int tweetId) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT author_id FROM tweets WHERE id = ?")) {
            stmt.setInt(1, tweetId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    /** Functional hook so each read can bind its own WHERE/LIMIT params (indexes 3+). */
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    /**
     * Runs a viewer-aware read: params 1 and 2 are always the viewerId (for the
     * liked/retweeted EXISTS checks), the binder sets the rest, then media and
     * hashtags are attached in bulk.
     */
    private List<Tweet> query(String sql, int viewerId, Binder binder) throws SQLException {
        List<Tweet> tweets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, viewerId);
            stmt.setInt(2, viewerId);
            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tweets.add(mapRow(rs));
                }
            }
        }
        attachMediaAndHashtags(tweets);
        return tweets;
    }

    private void attachMediaAndHashtags(List<Tweet> tweets) throws SQLException {
        if (tweets.isEmpty()) {
            return;
        }
        List<Integer> ids = new ArrayList<>();
        for (Tweet t : tweets) {
            ids.add(t.getId());
        }
        Map<Integer, List<String>> media = mediaDAO.getForTweets(ids);
        Map<Integer, List<String>> tags = hashtagDAO.getForTweets(ids);
        for (Tweet t : tweets) {
            t.setMedia(media.getOrDefault(t.getId(), new ArrayList<>()));
            t.setHashtags(tags.getOrDefault(t.getId(), new ArrayList<>()));
        }
    }

    private Tweet mapRow(ResultSet rs) throws SQLException {
        Tweet t = new Tweet();
        t.setId(rs.getInt("id"));
        t.setAuthorId(rs.getInt("author_id"));
        t.setAuthorUsername(rs.getString("author_username"));
        t.setAuthorDisplayName(rs.getString("author_display_name"));
        t.setAuthorAvatarUrl(rs.getString("author_avatar"));
        t.setContent(rs.getString("content"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        t.setCreatedAt(createdAt.toLocalDateTime().toString());

        int parentId = rs.getInt("parent_tweet_id");
        t.setParentTweetId(rs.wasNull() ? null : parentId);

        int marker = rs.getInt("retweet_marker");
        if (!rs.wasNull()) {
            t.setRetweetOf(marker);
            t.setRetweetedBy(rs.getString("retweeted_by"));
        }

        t.setLikeCount(rs.getInt("like_count"));
        t.setReplyCount(rs.getInt("reply_count"));
        t.setRetweetCount(rs.getInt("retweet_count"));
        t.setLiked(rs.getBoolean("liked"));
        t.setRetweeted(rs.getBoolean("retweeted"));
        return t;
    }
}
