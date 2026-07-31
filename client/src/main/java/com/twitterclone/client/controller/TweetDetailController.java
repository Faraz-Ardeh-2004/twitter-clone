package com.twitterclone.client.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.client.util.SceneNavigator;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 3 - Day 9-11
 * ============================================================
 * Controller for the tweet detail page + flat list of replies.
 *
 * NOTE: assumes GET_TWEET's response payload is
 * {"tweet": {...}, "replies": [...]} - coordinate with Hesam if he settles
 * on a different shape (e.g. replies as a separate packet type).
 */
public class TweetDetailController {

    @FXML
    private Button backButton;

    @FXML
    private VBox mainTweetContainer;

    @FXML
    private VBox repliesContainer;

    private Tweet mainTweet;

    private final Gson gson = new Gson();

    public void loadTweet(Tweet tweet) {
        this.mainTweet = tweet;

        try {
            mainTweetContainer.getChildren().setAll(createTweetCardNode(tweet));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("tweetId", tweet.getId());
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_TWEET, UserContext.getInstance().getToken(), payload),
                response -> {
                    if (!"OK".equals(response.getStatus()) || response.getPayload() == null) {
                        return;
                    }
                    Tweet[] replies = gson.fromJson(response.getPayload().get("replies"), Tweet[].class);
                    repliesContainer.getChildren().clear();
                    for (Tweet reply : replies) {
                        try {
                            repliesContainer.getChildren().add(createTweetCardNode(reply));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private Node createTweetCardNode(Tweet tweet) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TweetCard.fxml"));
        Node node = loader.load();
        TweetCardController controller = loader.getController();
        controller.setTweet(tweet);
        return node;
    }

    @FXML
    private void onBackButtonClick() {
        SceneNavigator.switchScene(backButton, "/fxml/Dashboard.fxml");
    }
}
