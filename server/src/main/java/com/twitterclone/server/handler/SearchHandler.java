package com.twitterclone.server.handler;

import com.twitterclone.server.db.SearchDAO;
import com.twitterclone.shared.protocol.Packet;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 4 - Day 12-14
 * ============================================================
 * Expected payload shape: { "query": "..." }
 */
public class SearchHandler {

    private static final SearchDAO searchDAO = new SearchDAO();

    private SearchHandler() {
    }

    /** TODO(Hesam): read query from the payload, call searchDAO.searchUsers(query), serialize the result. */
    public static Packet handleSearchUsers(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleSearchUsers in Phase 4");
    }

    /** TODO(Hesam): same as above but calls searchDAO.searchTweets(query). */
    public static Packet handleSearchTweets(Packet request) {
        throw new UnsupportedOperationException("TODO(Hesam): implement handleSearchTweets in Phase 4");
    }
}
