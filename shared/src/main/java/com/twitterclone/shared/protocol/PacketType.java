package com.twitterclone.shared.protocol;

/**
 * ============================================================
 * Owner: all 3 people together (Phase 0 - Day 1 to 2)
 * ============================================================
 * This file is the "protocol contract" between client and server.
 *
 * WARNING - hard rule: from the end of Day 2 onward, changing, removing, or
 * renaming any of these values requires agreement from all 3 people. If a
 * new type is needed (e.g. for a bonus feature), announce it in the group
 * immediately, and only after that should Hesam and Faraz start using it.
 *
 * Each enum value represents a JSON message with a "type" field equal to its
 * own name. Example of an actual request sent over the wire:
 *   { "type": "LOGIN", "token": null, "payload": { "username": "...", "password": "..." } }
 *
 * TODO(everyone): if a new feature needs a new type (e.g. a bonus feature),
 * add it here and also record it in the "owner" column of Report.md.
 */
public enum PacketType {

    // ---------- generic / infrastructure (Phase 0) ----------
    PING,           // client -> server: connectivity test
    PONG,           // server -> client: connectivity test response
    ERROR,          // server -> client: generic error (when no type matched)

    // ---------- authentication (Phase 1 - logic owner: Hesam) ----------
    REGISTER,
    LOGIN,
    LOGOUT,

    // ---------- tweets and feed (Phase 2 - logic owner: Hesam) ----------
    CREATE_TWEET,
    DELETE_TWEET,
    GET_TWEET,
    GET_USER_TWEETS,
    GET_FEED,
    NEW_TWEET_PUSH, // server -> client (unsolicited): a new tweet delivered in real time

    // ---------- social graph and interactions (Phase 3 - logic owner: Hesam) ----------
    FOLLOW,
    UNFOLLOW,
    GET_FOLLOWERS,
    GET_FOLLOWING,
    LIKE_TWEET,
    UNLIKE_TWEET,

    // ---------- retweets / reposts (Phase 3 bonus) ----------
    RETWEET,        // client -> server: repost (or quote) an existing tweet
    UNDO_RETWEET,   // client -> server: remove your repost of a tweet

    // ---------- profiles (Phase 1/3) ----------
    GET_PROFILE,    // client -> server: fetch a user's profile + stats (by userId or username)
    UPDATE_PROFILE, // client -> server: edit display name, bio, avatar, banner

    // ---------- search (Phase 4 - logic owner: Hesam + queries by AmirAli) ----------
    SEARCH_USERS,
    SEARCH_TWEETS,
    SEARCH_HASHTAG, // client -> server: tweets carrying a given hashtag
    TRENDING_HASHTAGS, // client -> server: most-used hashtags recently

    // ---------- notifications (bonus) ----------
    GET_NOTIFICATIONS,  // client -> server: list this user's notifications
    MARK_NOTIFICATIONS_READ,

    // ---------- real-time server pushes (server -> client, unsolicited) ----------
    LIKE_PUSH,          // a tweet you can see gained/lost a like
    FOLLOW_PUSH,        // someone followed/unfollowed you
    NOTIFICATION_PUSH   // a new notification for you
}
