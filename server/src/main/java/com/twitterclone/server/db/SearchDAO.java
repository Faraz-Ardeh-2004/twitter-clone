package com.twitterclone.server.db;

import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * ============================================================
 * Owner: AmirAli (Database) | Phase 4 - Day 12-14
 * ============================================================
 * Simple search queries using ILIKE (case-insensitive in Postgres).
 *
 * TODO(AmirAli):
 *  1. SELECT * FROM users WHERE username ILIKE '%' || ? || '%'
 *  2. SELECT * FROM tweets WHERE content ILIKE '%' || ? || '%' ORDER BY created_at DESC
 *  3. After implementing, check the indexes so database load doesn't degrade
 *     under concurrent testing (per the Phase 4 DoD; for free-text ILIKE a
 *     regular B-tree index doesn't help much - if you have time, read up on
 *     pg_trgm or full text search, otherwise simple search is fine for this project).
 */
public class SearchDAO {

    public List<User> searchUsers(String query) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement searchUsers in Phase 4");
    }

    public List<Tweet> searchTweets(String query) throws SQLException {
        throw new UnsupportedOperationException("TODO(AmirAli): implement searchTweets in Phase 4");
    }
}
