package com.twitterclone.client.controller;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 2 (display) + Phase 3 (like/reply)
 * ============================================================
 * Controller for a single TweetCard. DashboardController calls setTweet after
 * loading the fxml so this card has its own data.
 */
public class TweetCardController {

    @FXML
    private Label authorLabel;

    @FXML
    private Label timestampLabel;

    @FXML
    private Label contentLabel;

    @FXML
    private Button likeButton;

    @FXML
    private Button replyButton;

    private Tweet tweet;

    /**
     * TODO(Faraz - Phase 2): DashboardController calls this method right
     * after building the card. Populate authorLabel/timestampLabel/contentLabel
     * from tweet, and also set
     * likeButton.setText("Like (" + tweet.getLikeCount() + ")").
     */
    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
        // TODO(Faraz): full implementation per the guide above.
    }

    /**
     * TODO(Faraz - Phase 3): per the spec - "on click, a like packet is sent
     * to the server and the like count optimistically goes up by one" (i.e.
     * optimistic UI update - don't wait for the server's response, bump the
     * number locally right away).
     * Packet: Packet.request(PacketType.LIKE_TWEET, token, {"tweetId": tweet.getId()})
     */
    @FXML
    private void onLikeButtonClick() {
        // TODO(Faraz): full implementation per the guide above.
    }

    /**
     * TODO(Faraz - Phase 3): send a CREATE_TWEET packet with parentTweetId
     * filled in (after getting the reply text from the user - you can use a
     * simple JavaFX TextInputDialog, no separate fxml needed).
     */
    @FXML
    private void onReplyButtonClick() {
        // TODO(Faraz): full implementation per the guide above.
    }

    /**
     * TODO(Faraz - Phase 3): clicking the card itself (not the buttons)
     * should take the user to TweetDetail.fxml (per the spec: "simplest
     * possible version... remove the complex tree structure"). You can add
     * an onMouseClicked to the main VBox in the fxml that calls this method.
     */
    private void onCardClicked() {
        // TODO(Faraz): full implementation in Phase 3.
    }
}
