package com.twitterclone.client.ui;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.client.util.UiUtils;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;

/**
 * A self-contained tweet card. Renders author, content (with clickable
 * hashtags), media thumbnails, and an action row (reply / retweet / like /
 * delete), and performs the like/retweet/delete network calls itself.
 *
 * The card exposes its tweet id and an {@link #applyLikeUpdate} hook so a page
 * can update its like count live when a LIKE_PUSH arrives.
 */
public class TweetCard extends VBox {

    private final Tweet tweet;
    private final Navigator navigator;
    private final Consumer<Integer> onDeleted;

    private Button likeButton;
    private Button retweetButton;

    public TweetCard(Tweet tweet, Navigator navigator, Consumer<Integer> onDeleted) {
        this.tweet = tweet;
        this.navigator = navigator;
        this.onDeleted = onDeleted;
        getStyleClass().add("tweet-card");
        setSpacing(6);
        build();
        setOnMouseClicked(e -> {
            // Open detail only when the click wasn't on an interactive child.
            if (!(e.getTarget() instanceof Button) && !(e.getTarget() instanceof Text)
                    && !(e.getTarget() instanceof ImageView)) {
                navigator.openTweetDetail(tweet);
            }
        });
    }

    public int getTweetId() {
        return tweet.getId();
    }

    private void build() {
        getChildren().clear();

        // Retweet context banner
        if (tweet.getRetweetOf() != null && tweet.getRetweetedBy() != null) {
            Label ctx = new Label("@" + tweet.getRetweetedBy() + " retweeted");
            ctx.getStyleClass().add("tweet-context");
            ctx.setGraphic(Icons.create("repeat", 13, "icon-muted"));
            ctx.setGraphicTextGap(6);
            HBox ctxRow = new HBox(ctx);
            ctxRow.setPadding(new Insets(0, 0, 0, 54));
            getChildren().add(ctxRow);
        }

        HBox row = new HBox(10);
        Node avatar = UiUtils.avatar(displayName(), tweet.getAuthorAvatarUrl(), 44);
        avatar.setOnMouseClicked(e -> { e.consume(); navigator.openProfile(tweet.getAuthorId()); });

        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        // Header: display name · @handle · time
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(displayName());
        name.getStyleClass().add("tweet-author");
        Label handle = new Label("@" + tweet.getAuthorUsername());
        handle.getStyleClass().add("tweet-handle");
        Label dot = new Label("·");
        dot.getStyleClass().add("tweet-handle");
        Label time = new Label(UiUtils.relativeTime(tweet.getCreatedAt()));
        time.getStyleClass().add("tweet-time");
        header.getChildren().addAll(name, handle, dot, time);

        body.getChildren().add(header);

        // Content with clickable hashtags
        if (tweet.getContent() != null && !tweet.getContent().isBlank()) {
            body.getChildren().add(buildContent(tweet.getContent()));
        }

        // Media thumbnails
        if (tweet.getMedia() != null && !tweet.getMedia().isEmpty()) {
            body.getChildren().add(buildMedia());
        }

        body.getChildren().add(buildActions());

        row.getChildren().addAll(avatar, body);
        getChildren().add(row);
    }

    private TextFlow buildContent(String content) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("tweet-content");
        // Split into hashtag and non-hashtag runs.
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("#\\w+").matcher(content);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                flow.getChildren().add(plain(content.substring(last, m.start())));
            }
            String tag = m.group();
            Text t = new Text(tag);
            t.getStyleClass().add("hashtag");
            t.setOnMouseClicked(e -> { e.consume(); navigator.openHashtag(tag.substring(1)); });
            flow.getChildren().add(t);
            last = m.end();
        }
        if (last < content.length()) {
            flow.getChildren().add(plain(content.substring(last)));
        }
        return flow;
    }

    private Text plain(String s) {
        Text t = new Text(s);
        t.getStyleClass().add("tweet-content");
        return t;
    }

    private Node buildMedia() {
        FlowPane pane = new FlowPane(8, 8);
        for (String data : tweet.getMedia()) {
            Image img = UiUtils.decodeImage(data);
            if (img == null) {
                continue;
            }
            ImageView view = new ImageView(img);
            view.setFitWidth(220);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            view.setStyle("-fx-cursor: hand;");
            view.setOnMouseClicked(e -> { e.consume(); ImageViewer.show(img); });
            pane.getChildren().add(view);
        }
        return pane;
    }

    private Node buildActions() {
        HBox actions = new HBox(24);
        actions.setPadding(new Insets(6, 0, 0, 54));
        actions.setAlignment(Pos.CENTER_LEFT);

        Button reply = action("message-circle", "icon-muted", String.valueOf(tweet.getReplyCount()));
        reply.setOnAction(e -> navigator.openTweetDetail(tweet));

        retweetButton = new Button();
        retweetButton.getStyleClass().add("action-button");
        retweetButton.setContentDisplay(ContentDisplay.LEFT);
        retweetButton.setGraphicTextGap(6);
        refreshRetweetButton();
        retweetButton.setOnAction(e -> toggleRetweet());

        likeButton = new Button();
        likeButton.getStyleClass().add("action-button");
        likeButton.setContentDisplay(ContentDisplay.LEFT);
        likeButton.setGraphicTextGap(6);
        refreshLikeButton();
        likeButton.setOnAction(e -> toggleLike());

        actions.getChildren().addAll(reply, retweetButton, likeButton);

        if (tweet.getAuthorId() == UserContext.getInstance().getUserId() && tweet.getRetweetOf() == null) {
            Button delete = action("trash", "icon-muted", "");
            delete.setOnAction(e -> deleteTweet());
            actions.getChildren().add(delete);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        actions.getChildren().add(spacer);
        return actions;
    }

    private Button action(String iconName, String colorClass, String text) {
        Button b = new Button(text);
        b.getStyleClass().add("action-button");
        b.setGraphic(Icons.create(iconName, 18, colorClass));
        b.setContentDisplay(ContentDisplay.LEFT);
        b.setGraphicTextGap(6);
        return b;
    }

    // ---- interactions ----

    private void toggleLike() {
        boolean liking = !tweet.isLiked();
        // optimistic update
        tweet.setLiked(liking);
        tweet.setLikeCount(Math.max(0, tweet.getLikeCount() + (liking ? 1 : -1)));
        refreshLikeButton();

        JsonObject payload = new JsonObject();
        payload.addProperty("tweetId", tweet.getId());
        PacketType type = liking ? PacketType.LIKE_TWEET : PacketType.UNLIKE_TWEET;
        NetworkService.getInstance().sendRequest(
                Packet.request(type, null, payload),
                response -> {
                    if (!"OK".equals(response.getStatus())) {
                        // revert
                        tweet.setLiked(!liking);
                        tweet.setLikeCount(Math.max(0, tweet.getLikeCount() + (liking ? -1 : 1)));
                        refreshLikeButton();
                    } else if (response.getPayload() != null && response.getPayload().has("likeCount")) {
                        tweet.setLikeCount(response.getPayload().get("likeCount").getAsInt());
                        refreshLikeButton();
                    }
                });
    }

    private void toggleRetweet() {
        boolean retweeting = !tweet.isRetweeted();
        tweet.setRetweeted(retweeting);
        tweet.setRetweetCount(Math.max(0, tweet.getRetweetCount() + (retweeting ? 1 : -1)));
        refreshRetweetButton();

        JsonObject payload = new JsonObject();
        payload.addProperty("tweetId", tweet.getId());
        PacketType type = retweeting ? PacketType.RETWEET : PacketType.UNDO_RETWEET;
        NetworkService.getInstance().sendRequest(
                Packet.request(type, null, payload),
                response -> {
                    if (!"OK".equals(response.getStatus())) {
                        tweet.setRetweeted(!retweeting);
                        tweet.setRetweetCount(Math.max(0, tweet.getRetweetCount() + (retweeting ? -1 : 1)));
                        refreshRetweetButton();
                    } else if (response.getPayload() != null && response.getPayload().has("retweetCount")) {
                        tweet.setRetweetCount(response.getPayload().get("retweetCount").getAsInt());
                        refreshRetweetButton();
                    }
                });
    }

    private void deleteTweet() {
        JsonObject payload = new JsonObject();
        payload.addProperty("tweetId", tweet.getId());
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.DELETE_TWEET, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && onDeleted != null) {
                        onDeleted.accept(tweet.getId());
                    }
                });
    }

    /** Called by a page when a LIKE_PUSH for this tweet arrives. */
    public void applyLikeUpdate(int likeCount) {
        tweet.setLikeCount(likeCount);
        refreshLikeButton();
    }

    private void refreshLikeButton() {
        likeButton.setText(String.valueOf(tweet.getLikeCount()));
        likeButton.setGraphic(Icons.create("heart", 18, tweet.isLiked() ? "icon-like" : "icon-muted"));
        likeButton.getStyleClass().remove("liked");
        if (tweet.isLiked()) {
            likeButton.getStyleClass().add("liked");
        }
    }

    private void refreshRetweetButton() {
        retweetButton.setText(String.valueOf(tweet.getRetweetCount()));
        retweetButton.setGraphic(Icons.create("repeat", 18, tweet.isRetweeted() ? "icon-retweet" : "icon-muted"));
        retweetButton.getStyleClass().remove("retweeted");
        if (tweet.isRetweeted()) {
            retweetButton.getStyleClass().add("retweeted");
        }
    }

    private String displayName() {
        String d = tweet.getAuthorDisplayName();
        return (d == null || d.isBlank()) ? tweet.getAuthorUsername() : d;
    }
}
