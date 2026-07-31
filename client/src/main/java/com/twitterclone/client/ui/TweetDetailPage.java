package com.twitterclone.client.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Arrays;

/**
 * A tweet's detail view: the tweet itself, a reply composer, and the flat list
 * of replies (a threaded conversation). The tweet is always (re)fetched by id so
 * this page works even when opened from just an id (e.g. from a notification).
 */
public class TweetDetailPage {

    private final Gson gson = new Gson();
    private final Navigator navigator;
    private final int tweetId;
    private final VBox root = new VBox();
    private final VBox content = new VBox();
    private final FeedList replies;

    public TweetDetailPage(Navigator navigator, int tweetId) {
        this.navigator = navigator;
        this.tweetId = tweetId;
        this.replies = new FeedList(navigator);
        this.replies.setEmptyText("No replies yet. Be the first to reply!");

        Label title = new Label("Tweet");
        title.getStyleClass().add("title");
        Button back = new Button("← Back");
        back.getStyleClass().add("action-button");
        back.setOnAction(e -> navigator.openHome());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, back, title);
        header.setPadding(new Insets(14, 16, 10, 16));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        load();
    }

    public Node getView() {
        return root;
    }

    private void load() {
        JsonObject payload = new JsonObject();
        payload.addProperty("tweetId", tweetId);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_TWEET, null, payload),
                response -> {
                    if (!"OK".equals(response.getStatus()) || response.getPayload() == null) {
                        content.getChildren().setAll(errorLabel());
                        return;
                    }
                    Tweet main = gson.fromJson(response.getPayload().get("tweet"), Tweet.class);
                    Tweet[] replyArr = gson.fromJson(response.getPayload().get("replies"), Tweet[].class);

                    TweetCard mainCard = new TweetCard(main, navigator, id -> navigator.openHome());
                    Composer replyComposer = new Composer(tweetId, "Reply", "Tweet your reply",
                            replies::prepend);

                    Label repliesHeading = new Label("Replies");
                    repliesHeading.getStyleClass().add("h2");
                    repliesHeading.setPadding(new Insets(8, 16, 4, 16));

                    replies.render(Arrays.asList(replyArr));
                    content.getChildren().setAll(mainCard, replyComposer, repliesHeading, replies);
                });
    }

    private Label errorLabel() {
        Label l = new Label("This tweet is unavailable.");
        l.getStyleClass().add("text-muted");
        l.setPadding(new Insets(24));
        return l;
    }
}
