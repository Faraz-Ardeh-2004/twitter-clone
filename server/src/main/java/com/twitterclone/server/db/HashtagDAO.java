package com.twitterclone.server.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Access to the {@code hashtags} and {@code tweet_hashtags} tables.
 * Hashtags are stored lower-cased, without the leading '#'.
 */
public class HashtagDAO {

    /**
     * Upserts each tag and links it to the tweet, on the given connection so it
     * participates in the tweet-creation transaction. Uses INSERT ... ON
     * CONFLICT so concurrent inserts of the same tag are safe.
     */
    public void attach(Connection conn, int tweetId, List<String> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        String upsert = "INSERT INTO hashtags (tag) VALUES (?) " +
                "ON CONFLICT (tag) DO UPDATE SET tag = EXCLUDED.tag RETURNING id";
        String link = "INSERT INTO tweet_hashtags (tweet_id, hashtag_id) VALUES (?, ?) " +
                "ON CONFLICT DO NOTHING";
        try (PreparedStatement up = conn.prepareStatement(upsert);
             PreparedStatement lk = conn.prepareStatement(link)) {
            for (String tag : tags) {
                up.setString(1, tag);
                int hashtagId;
                try (ResultSet rs = up.executeQuery()) {
                    rs.next();
                    hashtagId = rs.getInt(1);
                }
                lk.setInt(1, tweetId);
                lk.setInt(2, hashtagId);
                lk.addBatch();
            }
            lk.executeBatch();
        }
    }

    /** Bulk-loads hashtags for a set of tweet ids: tweetId -> list of tags. */
    public Map<Integer, List<String>> getForTweets(List<Integer> tweetIds) throws SQLException {
        Map<Integer, List<String>> result = new HashMap<>();
        if (tweetIds == null || tweetIds.isEmpty()) {
            return result;
        }
        String sql = "SELECT th.tweet_id, h.tag FROM tweet_hashtags th " +
                "JOIN hashtags h ON th.hashtag_id = h.id WHERE th.tweet_id = ANY(?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            Array arr = conn.createArrayOf("integer", tweetIds.toArray());
            stmt.setArray(1, arr);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.computeIfAbsent(rs.getInt("tweet_id"), k -> new ArrayList<>())
                            .add(rs.getString("tag"));
                }
            }
        }
        return result;
    }

    /** Most-used hashtags across all tweets, most popular first. tag -> count. */
    public List<Map.Entry<String, Integer>> trending(int limit) throws SQLException {
        String sql = "SELECT h.tag, COUNT(*) AS cnt FROM tweet_hashtags th " +
                "JOIN hashtags h ON th.hashtag_id = h.id " +
                "GROUP BY h.tag ORDER BY cnt DESC, h.tag ASC LIMIT ?";
        List<Map.Entry<String, Integer>> out = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(new AbstractMap.SimpleEntry<>(rs.getString("tag"), rs.getInt("cnt")));
                }
            }
        }
        return out;
    }
}
