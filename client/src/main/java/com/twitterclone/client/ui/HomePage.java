package com.twitterclone.client.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The home timeline: a composer on top and, below it, the personalized feed
 * (own tweets + tweets/retweets from followed users). A toggle switches between
 * the personalized feed and a global "Explore" feed. Real-time NEW_TWEET_PUSH
 * and LIKE_PUSH updates are applied live.
 *
 * A single HomePage instance lives for the app's lifetime (see AppShell), so its
 * push listeners stay valid across navigation.
 */
public class HomePage {

    private final Gson gson = new Gson();
    private final FeedList feed;
    private final VBox root;
    private boolean global = false;
    private Button toggleButton;

    public HomePage(Navigator navigator) {
        feed = new FeedList(navigator);
        feed.setEmptyText("No tweets yet. Follow people or post the first tweet!");

        Label title = new Label("Home");
        title.getStyleClass().add("title");
        toggleButton = new Button("Following");
        toggleButton.getStyleClass().add("btn-outline");
        toggleButton.setOnAction(e -> {
            global = !global;
            toggleButton.setText(global ? "Explore" : "Following");
            load();
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, title, spacer, toggleButton);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setAlignment(Pos.CENTER_LEFT);

        Composer composer = new Composer(null, "Tweet", "What's happening?", feed::prepend);

        ScrollPane scroll = new ScrollPane(feed);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root = new VBox(header, composer, new Separator(), scroll);

        // Real-time listeners (registered once for the app lifetime).
        NetworkService.getInstance().addPushListener(PacketType.NEW_TWEET_PUSH.name(), this::onNewTweet);
        NetworkService.getInstance().addPushListener(PacketType.LIKE_PUSH.name(), this::onLikePush);
        // The initial feed load is triggered by AppShell.openHome().
    }

    public Node getView() {
        return root;
    }

    public void load() {
        JsonObject payload = new JsonObject();
        payload.addProperty("limit", 40);
        payload.addProperty("offset", 0);
        payload.addProperty("global", global);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_FEED, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        Tweet[] tweets = gson.fromJson(response.getPayload().get("tweets"), Tweet[].class);
                        feed.render(java.util.Arrays.asList(tweets));
                    }
                });
    }

    private void onNewTweet(Packet packet) {
        if (global) {
            // In global mode a full reload keeps ordering correct; keep it simple.
            load();
            return;
        }
        Tweet tweet = gson.fromJson(packet.getPayload(), Tweet.class);
        feed.prepend(tweet);
    }

    private void onLikePush(Packet packet) {
        JsonObject p = packet.getPayload();
        if (p != null && p.has("tweetId") && p.has("likeCount")) {
            feed.applyLikePush(p.get("tweetId").getAsInt(), p.get("likeCount").getAsInt());
        }
    }

    /** Thin visual separator styled by CSS. */
    private static class Separator extends Region {
        Separator() {
            setMinHeight(1);
            setStyle("-fx-background-color: -fx-border;");
        }
    }
}
