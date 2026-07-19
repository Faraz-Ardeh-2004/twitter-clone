package com.twitterclone.client.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 2 (Day 6-8) base + Phase 3/4 (interactions/search)
 * ============================================================
 * The most important controller in the project as far as real-time goes: it
 * must both load the initial feed from the server and, after that, listen
 * for push packets (NEW_TWEET_PUSH).
 */
public class DashboardController {

    @FXML
    private TextArea tweetContentField;

    @FXML
    private Button postTweetButton;

    @FXML
    private VBox feedContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    private final Gson gson = new Gson();

    /**
     * TODO(Faraz - Phase 2):
     * This method is called automatically when the FXMLLoader builds the
     * scene (because JavaFX recognizes "initialize" as a lifecycle hook - as
     * long as you keep this exact signature, you don't need to call it manually).
     *
     * What needs to happen here:
     *  1. Send a GET_FEED request (payload: {"limit": 20, "offset": 0}) and,
     *     in the callback, build a TweetCard for each returned Tweet and add
     *     it to feedContainer.
     *  2. Call NetworkService.getInstance().setPushListener(this::onNewTweetPush)
     *     so this controller is registered for real-time pushes.
     */
    @FXML
    public void initialize() {
        // TODO(Faraz): full implementation per the guide above.
    }

    /**
     * TODO(Faraz - Phase 2):
     *  1. Read the text from tweetContentField; if empty, do nothing.
     *  2. payload = {"content": "...", "parentTweetId": null}
     *  3. Send Packet.request(PacketType.CREATE_TWEET,
     *     UserContext.getInstance().getToken(), payload).
     *  4. On a successful callback: clear tweetContentField (the new card
     *     will be added to the feed via the push, so there's no need to add
     *     it manually here - confirm this with Hesam, since the server also
     *     pushes back to the sender).
     */
    @FXML
    private void onPostTweetClick() {
        // TODO(Faraz): full implementation per the guide above.
    }

    /**
     * TODO(Faraz - Phase 2): this method is called by NetworkService whenever
     * a NEW_TWEET_PUSH packet arrives (already on the JavaFX thread, since
     * NetworkService calls it via Platform.runLater - no extra runLater needed here).
     *
     * Steps:
     *  1. Convert packet.getPayload() into a Tweet using Gson.
     *  2. Build a new node from TweetCard.fxml (see the createTweetCardNode helper below).
     *  3. feedContainer.getChildren().add(0, node) - i.e. the top of the feed.
     */
    private void onNewTweetPush(Packet packet) {
        // TODO(Faraz): full implementation per the guide above.
    }

    /**
     * TODO(Faraz): a helper that takes a Tweet and returns a Node (built from
     * TweetCard.fxml) so both initialize and onNewTweetPush can use it
     * instead of duplicating code.
     * Pattern:
     *   FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TweetCard.fxml"));
     *   Node node = loader.load();
     *   TweetCardController controller = loader.getController();
     *   controller.setTweet(tweet);
     *   return node;
     */
    private Node createTweetCardNode(Tweet tweet) throws IOException {
        throw new UnsupportedOperationException("TODO(Faraz): implement createTweetCardNode in Phase 2");
    }

    /**
     * TODO(Faraz - Phase 4): read the searchField text, and depending on
     * whether the user wants to find a user or a tweet (you can send both
     * SEARCH_USERS and SEARCH_TWEETS at once), show the result in a simple
     * page/popup.
     */
    @FXML
    private void onSearchButtonClick() {
        // TODO(Faraz): full implementation in Phase 4.
    }
}
