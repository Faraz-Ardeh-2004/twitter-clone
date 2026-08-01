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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Access to the {@code hashtags} and {@code tweet_hashtags} tables.
 */
public class HashtagDAO {

    public void attach(Connection conn, int tweetId, List<String> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        String upsert = "INSERT INTO hashtags (tag) VALUES (?) " +
                "ON CONFLICT (LOWER(tag)) DO NOTHING " +
                "RETURNING id";

        String link = "INSERT INTO tweet_hashtags (tweet_id, hashtag_id) VALUES (?, ?) " +
                "ON CONFLICT DO NOTHING";
        try (PreparedStatement up = conn.prepareStatement(upsert);
             PreparedStatement lk = conn.prepareStatement(link)) {
            for (String tag : tags) {
                up.setString(1, tag);
                int hashtagId;
                try (ResultSet rs = up.executeQuery()) {
                    if (rs.next()) {
                        hashtagId = rs.getInt(1);
                    } else {
                        hashtagId = getHashtagId(conn, tag);
                    }
                }
                lk.setInt(1, tweetId);
                lk.setInt(2, hashtagId);
                lk.addBatch();
            }
            lk.executeBatch();
        }
    }

    private int getHashtagId(Connection conn, String tag) throws SQLException {
        String sql = "SELECT id FROM hashtags WHERE LOWER(tag) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Could not retrieve hashtag ID for: " + tag);
    }

    /**
     * Finds the most frequently used casing for a hashtag by scanning
     * the actual tweet contents.
     */
    public String getCanonicalTag(String rawTag) throws SQLException {
        String sql = "SELECT t.content FROM tweets t " +
                "JOIN tweet_hashtags th ON t.id = th.tweet_id " +
                "JOIN hashtags h ON th.hashtag_id = h.id " +
                "WHERE LOWER(h.tag) = LOWER(?)";

        Map<String, Integer> casingCounts = new HashMap<>();
        Pattern pattern = Pattern.compile("#(" + Pattern.quote(rawTag) + ")", Pattern.CASE_INSENSITIVE);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rawTag);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String content = rs.getString("content");
                    Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        String foundTag = matcher.group(1);
                        casingCounts.put(foundTag, casingCounts.getOrDefault(foundTag, 0) + 1);
                    }
                }
            }
        }

        return casingCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(rawTag);
    }

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

    public List<Map.Entry<String, Integer>> trending(int limit) throws SQLException {
        String sql = "SELECT h.tag, COUNT(*) AS cnt FROM tweet_hashtags th " +
                "JOIN hashtags h ON th.hashtag_id = h.id " +
                "GROUP BY h.id, h.tag ORDER BY cnt DESC LIMIT ?";
        List<Map.Entry<String, Integer>> out = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(new AbstractMap.SimpleEntry<>(getCanonicalTag(rs.getString("tag")), rs.getInt("cnt")));
                }
            }
        }
        return out;
    }
}