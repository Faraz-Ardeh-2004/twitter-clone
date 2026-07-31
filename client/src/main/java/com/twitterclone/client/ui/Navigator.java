package com.twitterclone.client.ui;

import com.twitterclone.shared.model.Tweet;

/**
 * Navigation callbacks a tweet card (or any page) can trigger to move around the
 * app. Implemented by {@link AppShell}.
 */
public interface Navigator {

    void openHome();

    void openProfile(int userId);

    void openProfileByUsername(String username);

    void openTweetDetail(Tweet tweet);

    void openTweetById(int tweetId);

    void openHashtag(String tag);

    void openSearch(String query);

    void openNotifications();
}
