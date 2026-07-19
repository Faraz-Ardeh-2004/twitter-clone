package com.twitterclone.client.controller;

import com.twitterclone.client.network.NetworkService;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 3 - Day 9-11
 * ============================================================
 * Controller for the tweet detail page + flat list of replies.
 */
public class TweetDetailController {

    @FXML
    private Button backButton;

    @FXML
    private VBox mainTweetContainer;

    @FXML
    private VBox repliesContainer;

    private Tweet mainTweet;

    /**
     * TODO(Faraz - Phase 3): TweetCardController.onCardClicked calls this
     * method (after building this scene from the fxml) so it knows which
     * tweet to show.
     *  1. Store mainTweet, build a TweetCard for it and add it to mainTweetContainer.
     *  2. Send a GET_TWEET request or a dedicated replies endpoint (coordinate
     *     with Hesam on whether replies come as part of the GET_TWEET
     *     response or as a separate type).
     *  3. For each reply, build a TweetCard and add it to repliesContainer.
     */
    public void loadTweet(Tweet tweet) {
        this.mainTweet = tweet;
        // TODO(Faraz): full implementation per the guide above.
    }

    /** TODO(Faraz): send the user back to Dashboard.fxml (same FXMLLoader pattern). */
    @FXML
    private void onBackButtonClick() {
        // TODO(Faraz): full implementation per the guide above.
    }
}
