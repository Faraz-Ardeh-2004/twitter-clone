package com.twitterclone.server.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Access to the {@code media} table (base64 image data attached to tweets).
 * Insertion takes an existing Connection so it can run inside the same
 * transaction as the tweet insert (see TweetDAO.createTweet).
 */
public class MediaDAO {

    /** Inserts all images for a tweet, preserving order, on the given connection. */
    public void insert(Connection conn, int tweetId, List<String> images) throws SQLException {
        if (images == null || images.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO media (tweet_id, data, position) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int pos = 0;
            for (String data : images) {
                if (data == null || data.isBlank()) {
                    continue;
                }
                stmt.setInt(1, tweetId);
                stmt.setString(2, data);
                stmt.setInt(3, pos++);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /** Bulk-loads media for a set of tweet ids: tweetId -> ordered list of base64 images. */
    public Map<Integer, List<String>> getForTweets(List<Integer> tweetIds) throws SQLException {
        Map<Integer, List<String>> result = new HashMap<>();
        if (tweetIds == null || tweetIds.isEmpty()) {
            return result;
        }
        String sql = "SELECT tweet_id, data FROM media WHERE tweet_id = ANY(?) ORDER BY tweet_id, position";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            Array arr = conn.createArrayOf("integer", tweetIds.toArray());
            stmt.setArray(1, arr);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int tid = rs.getInt("tweet_id");
                    result.computeIfAbsent(tid, k -> new ArrayList<>()).add(rs.getString("data"));
                }
            }
        }
        return result;
    }
}
