package com.twitterclone.client.controller;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 2 (Day 6-8) base + Phase 3/4 (interactions/search)
 * ============================================================
 * The most important controller in the project as far as real-time goes: it
 * must both load the initial feed from the server and, after that, listen
 * for push packets (NEW_TWEET_PUSH).
 *
 * NOTE: assumes GET_FEED/SEARCH_USERS/SEARCH_TWEETS responses wrap their
 * results as payload = {"tweets": [...]} / {"users": [...]} - coordinate
 * with Hesam if he settles on a different shape.
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

    @FXML
    public void initialize() {
        loadFeed();
        NetworkService.getInstance().setPushListener(this::onNewTweetPush);
    }

    private void loadFeed() {
        JsonObject payload = new JsonObject();
        payload.addProperty("limit", 20);
        payload.addProperty("offset", 0);

        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_FEED, UserContext.getInstance().getToken(), payload),
                response -> {
                    if (!"OK".equals(response.getStatus()) || response.getPayload() == null) {
                        return;
                    }
                    Tweet[] tweets = gson.fromJson(response.getPayload().get("tweets"), Tweet[].class);
                    feedContainer.getChildren().clear();
                    for (Tweet tweet : tweets) {
                        addCardToFeed(tweet, feedContainer.getChildren().size());
                    }
                });
    }

    @FXML
    private void onPostTweetClick() {
        String content = tweetContentField.getText();
        if (content == null || content.isBlank()) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("content", content);
        payload.add("parentTweetId", JsonNull.INSTANCE);

        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.CREATE_TWEET, UserContext.getInstance().getToken(), payload),
                response -> {
                    if ("OK".equals(response.getStatus())) {
                        tweetContentField.clear();
                    }
                });
    }

    private void onNewTweetPush(Packet packet) {
        Tweet tweet = gson.fromJson(packet.getPayload(), Tweet.class);
        addCardToFeed(tweet, 0);
    }

    private void addCardToFeed(Tweet tweet, int index) {
        try {
            feedContainer.getChildren().add(index, createTweetCardNode(tweet));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Node createTweetCardNode(Tweet tweet) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TweetCard.fxml"));
        Node node = loader.load();
        TweetCardController controller = loader.getController();
        controller.setTweet(tweet);
        return node;
    }

    /**
     * Fires SEARCH_USERS and SEARCH_TWEETS at once and shows the combined
     * result in a simple popup once both responses are back.
     */
    @FXML
    private void onSearchButtonClick() {
        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("query", query);

        StringBuilder result = new StringBuilder();
        AtomicInteger pending = new AtomicInteger(2);

        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.SEARCH_USERS, UserContext.getInstance().getToken(), payload),
                response -> {
                    result.append("Users:\n");
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        User[] users = gson.fromJson(response.getPayload().get("users"), User[].class);
                        for (User user : users) {
                            result.append(" - @").append(user.getUsername()).append("\n");
                        }
                    }
                    if (pending.decrementAndGet() == 0) {
                        showSearchResultsPopup(result.toString());
                    }
                });

        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.SEARCH_TWEETS, UserContext.getInstance().getToken(), payload),
                response -> {
                    result.append("\nTweets:\n");
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        Tweet[] tweets = gson.fromJson(response.getPayload().get("tweets"), Tweet[].class);
                        for (Tweet tweet : tweets) {
                            result.append(" - @").append(tweet.getAuthorUsername())
                                    .append(": ").append(tweet.getContent()).append("\n");
                        }
                    }
                    if (pending.decrementAndGet() == 0) {
                        showSearchResultsPopup(result.toString());
                    }
                });
    }

    private void showSearchResultsPopup(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Search results");
        alert.setHeaderText(null);
        alert.setContentText(content.isBlank() ? "No results." : content);
        alert.showAndWait();
    }
}
