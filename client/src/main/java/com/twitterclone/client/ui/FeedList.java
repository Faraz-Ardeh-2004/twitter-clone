package com.twitterclone.client.ui;

import com.twitterclone.shared.model.Tweet;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A vertical list of {@link TweetCard}s that keeps a handle on each card by
 * tweet id, so pages can prepend new tweets, remove deleted ones, and apply
 * live like-count updates from LIKE_PUSH.
 */
public class FeedList extends VBox {

    private final Navigator navigator;
    private final java.util.Map<Integer, TweetCard> cards = new java.util.HashMap<>();
    private Label emptyLabel;

    public FeedList(Navigator navigator) {
        this.navigator = navigator;
        setFillWidth(true);
    }

    public void setEmptyText(String text) {
        emptyLabel = new Label(text);
        emptyLabel.getStyleClass().add("text-muted");
        emptyLabel.setStyle("-fx-padding: 24;");
    }

    public void render(List<Tweet> tweets) {
        getChildren().clear();
        cards.clear();
        if (tweets == null || tweets.isEmpty()) {
            if (emptyLabel != null) {
                getChildren().add(emptyLabel);
            }
            return;
        }
        for (Tweet t : tweets) {
            addCard(t, false);
        }
    }

    public void prepend(Tweet tweet) {
        // Avoid duplicates (e.g. echo of our own tweet) by id.
        if (cards.containsKey(tweet.getId()) && tweet.getRetweetOf() == null) {
            return;
        }
        addCard(tweet, true);
    }

    private void addCard(Tweet tweet, boolean top) {
        if (emptyLabel != null) {
            getChildren().remove(emptyLabel);
        }
        TweetCard card = new TweetCard(tweet, navigator, this::removeTweet);
        cards.put(tweet.getId(), card);
        if (top) {
            getChildren().add(0, card);
        } else {
            getChildren().add(card);
        }
    }

    public void removeTweet(int tweetId) {
        TweetCard card = cards.remove(tweetId);
        if (card != null) {
            getChildren().remove(card);
        }
    }

    public void applyLikePush(int tweetId, int likeCount) {
        TweetCard card = cards.get(tweetId);
        if (card != null) {
            card.applyLikeUpdate(likeCount);
        }
    }

    public List<Integer> tweetIds() {
        return new ArrayList<>(cards.keySet());
    }
}
