package com.twitterclone.server.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.twitterclone.server.db.HashtagDAO;
import com.twitterclone.server.db.SearchDAO;
import com.twitterclone.server.db.TweetDAO;
import com.twitterclone.server.util.Json;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Search across users, tweet text, and hashtags, plus trending hashtags.
 * Expected payload: { "query": "..." } (SEARCH_HASHTAG accepts the tag with or
 * without a leading '#').
 */
public class SearchHandler {

    private static final SearchDAO searchDAO = new SearchDAO();
    private static final TweetDAO tweetDAO = new TweetDAO();
    private static final HashtagDAO hashtagDAO = new HashtagDAO();

    private SearchHandler() {
    }

    public static Packet handleSearchUsers(Packet request, int userId) {
        String query = Json.getString(request.getPayload(), "query");
        if (query == null || query.isBlank()) {
            return Packet.error(PacketType.SEARCH_USERS, "Empty search query");
        }
        try {
            List<User> users = searchDAO.searchUsers(query.trim(), userId);
            JsonObject out = new JsonObject();
            out.add("users", Json.array(users));
            return Packet.ok(PacketType.SEARCH_USERS, out);
        } catch (SQLException e) {
            System.err.println("searchUsers failed: " + e.getMessage());
            return Packet.error(PacketType.SEARCH_USERS, "Search failed");
        }
    }

    public static Packet handleSearchTweets(Packet request, int userId) {
        String query = Json.getString(request.getPayload(), "query");
        if (query == null || query.isBlank()) {
            return Packet.error(PacketType.SEARCH_TWEETS, "Empty search query");
        }
        try {
            List<Tweet> tweets = tweetDAO.searchTweets(query.trim(), userId);
            JsonObject out = new JsonObject();
            out.add("tweets", Json.array(tweets));
            return Packet.ok(PacketType.SEARCH_TWEETS, out);
        } catch (SQLException e) {
            System.err.println("searchTweets failed: " + e.getMessage());
            return Packet.error(PacketType.SEARCH_TWEETS, "Search failed");
        }
    }

    public static Packet handleSearchHashtag(Packet request, int userId) {
        String query = Json.getString(request.getPayload(), "query");
        if (query == null || query.isBlank()) {
            return Packet.error(PacketType.SEARCH_HASHTAG, "Empty hashtag");
        }
        String tag = query.trim();
        if (tag.startsWith("#")) {
            tag = tag.substring(1);
        }
        try {
            List<Tweet> tweets = tweetDAO.getTweetsByHashtag(tag, userId);
            JsonObject out = new JsonObject();
            out.addProperty("hashtag", tag.toLowerCase());
            out.add("tweets", Json.array(tweets));
            return Packet.ok(PacketType.SEARCH_HASHTAG, out);
        } catch (SQLException e) {
            System.err.println("searchHashtag failed: " + e.getMessage());
            return Packet.error(PacketType.SEARCH_HASHTAG, "Search failed");
        }
    }

    public static Packet handleTrending(Packet request, int userId) {
        int limit = Json.getInt(request.getPayload(), "limit", 10);
        try {
            List<Map.Entry<String, Integer>> trending = hashtagDAO.trending(limit);
            JsonArray arr = new JsonArray();
            for (Map.Entry<String, Integer> e : trending) {
                JsonObject o = new JsonObject();
                o.addProperty("tag", e.getKey());
                o.addProperty("count", e.getValue());
                arr.add(o);
            }
            JsonObject out = new JsonObject();
            out.add("hashtags", arr);
            return Packet.ok(PacketType.TRENDING_HASHTAGS, out);
        } catch (SQLException e) {
            System.err.println("trending failed: " + e.getMessage());
            return Packet.error(PacketType.TRENDING_HASHTAGS, "Could not load trends");
        }
    }
}
