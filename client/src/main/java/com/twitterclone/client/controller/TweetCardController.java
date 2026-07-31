package com.twitterclone.client.controller;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

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

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
        authorLabel.setText("@" + tweet.getAuthorUsername());
        timestampLabel.setText(tweet.getCreatedAt());
        contentLabel.setText(tweet.getContent());
        likeButton.setText("Like (" + tweet.getLikeCount() + ")");
    }

    /** Optimistic UI: the like count bumps locally right away, before the server confirms. */
    @FXML
    private void onLikeButtonClick() {
        tweet.setLikeCount(tweet.getLikeCount() + 1);
        likeButton.setText("Like (" + tweet.getLikeCount() + ")");

        JsonObject payload = new JsonObject();
        payload.addProperty("tweetId", tweet.getId());
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.LIKE_TWEET, UserContext.getInstance().getToken(), payload),
                response -> {
                    if (!"OK".equals(response.getStatus())) {
                        // revert the optimistic bump if the server rejected the like
                        tweet.setLikeCount(tweet.getLikeCount() - 1);
                        likeButton.setText("Like (" + tweet.getLikeCount() + ")");
                    }
                });
    }

    @FXML
    private void onReplyButtonClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reply");
        dialog.setHeaderText("Replying to @" + tweet.getAuthorUsername());
        dialog.setContentText("Your reply:");

        dialog.showAndWait().ifPresent(replyContent -> {
            if (replyContent.isBlank()) {
                return;
            }
            JsonObject payload = new JsonObject();
            payload.addProperty("content", replyContent);
            payload.addProperty("parentTweetId", tweet.getId());
            NetworkService.getInstance().sendRequest(
                    Packet.request(PacketType.CREATE_TWEET, UserContext.getInstance().getToken(), payload),
                    response -> {
                        // the reply shows up in the parent tweet's detail page on next open;
                        // nothing to update in this card.
                    });
        });
    }

    /**
     * Wired from TweetCard.fxml's root VBox via onMouseClicked. Mouse events
     * bubble up from the Like/Reply buttons too, so ignore clicks that
     * originated on a Button - otherwise pressing Like would also navigate
     * to the detail page.
     */
    @FXML
    private void onCardClicked(MouseEvent event) {
        if (event.getTarget() instanceof Button) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TweetDetail.fxml"));
            Parent root = loader.load();
            TweetDetailController controller = loader.getController();
            controller.loadTweet(tweet);

            Stage stage = (Stage) contentLabel.getScene().getWindow();
            Scene currentScene = contentLabel.getScene();
            stage.setScene(new Scene(root, currentScene.getWidth(), currentScene.getHeight()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
