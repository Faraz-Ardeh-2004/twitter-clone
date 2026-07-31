package com.twitterclone.client.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.client.util.UiUtils;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A user's profile: banner, avatar, display name (+ verified badge), bio, and
 * follower/following/tweet stats, followed by that user's tweets and retweets.
 * Shows an "Edit profile" button for the current user, otherwise a
 * follow/unfollow button.
 */
public class ProfilePage {

    private final Gson gson = new Gson();
    private final Navigator navigator;
    private final int userId;
    private final VBox root = new VBox();
    private final VBox headerBox = new VBox();
    private final FeedList feed;

    private User profile;
    private boolean following;
    private Button followButton;
    private Label followerCountLabel;

    public ProfilePage(Navigator navigator, int userId) {
        this.navigator = navigator;
        this.userId = userId;
        this.feed = new FeedList(navigator);
        feed.setEmptyText("No tweets yet.");

        ScrollPane scroll = new ScrollPane(new VBox(headerBox, feed));
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);

        load();
    }

    public Node getView() {
        return root;
    }

    private void load() {
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", userId);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_PROFILE, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        profile = gson.fromJson(response.getPayload().get("user"), User.class);
                        following = profile.isFollowing();
                        renderHeader();
                        loadTweets();
                    }
                });
    }

    private void loadTweets() {
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", userId);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_USER_TWEETS, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        Tweet[] tweets = gson.fromJson(response.getPayload().get("tweets"), Tweet[].class);
                        feed.render(java.util.Arrays.asList(tweets));
                    }
                });
    }

    private void renderHeader() {
        headerBox.getChildren().clear();

        // Banner (image if set, else accent color)
        StackPane banner = new StackPane();
        banner.getStyleClass().add("profile-banner");
        var bannerImg = UiUtils.decodeImage(profile.getBannerUrl());
        if (bannerImg != null) {
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(bannerImg);
            iv.setFitHeight(140);
            iv.setPreserveRatio(true);
            banner.getChildren().add(iv);
        }

        Node avatar = UiUtils.avatar(profile.displayNameOrUsername(), profile.getAvatarUrl(), 80);

        // Action button row (Edit or Follow)
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(8, 16, 0, 16));
        boolean self = profile.getId() == UserContext.getInstance().getUserId();
        if (self) {
            Button edit = new Button("Edit profile");
            edit.getStyleClass().add("btn-outline");
            edit.setOnAction(e -> EditProfileDialog.show(root.getScene() == null ? null : root.getScene().getWindow(),
                    profile, this::onProfileUpdated));
            actionRow.getChildren().add(edit);
        } else {
            followButton = new Button();
            updateFollowButton();
            followButton.setOnAction(e -> toggleFollow());
            actionRow.getChildren().add(followButton);
        }

        VBox identity = new VBox(2);
        identity.setPadding(new Insets(4, 16, 8, 16));
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(profile.displayNameOrUsername());
        name.getStyleClass().add("title");
        nameRow.getChildren().add(name);
        if (profile.isVerified()) {
            Label badge = new Label("✔");
            badge.getStyleClass().add("verified");
            nameRow.getChildren().add(badge);
        }
        Label handle = new Label("@" + profile.getUsername());
        handle.getStyleClass().add("tweet-handle");
        identity.getChildren().addAll(nameRow, handle);
        if (profile.getBio() != null && !profile.getBio().isBlank()) {
            Label bio = new Label(profile.getBio());
            bio.getStyleClass().add("label");
            bio.setWrapText(true);
            identity.getChildren().add(bio);
        }

        // Stats
        HBox stats = new HBox(20);
        stats.setPadding(new Insets(4, 16, 12, 16));
        stats.getChildren().addAll(
                stat(String.valueOf(profile.getTweetCount()), "Tweets", null),
                stat(String.valueOf(profile.getFollowingCount()), "Following",
                        () -> UserListPage.open(navigator, root, userId, false)),
                followerStat());

        StackPane avatarWrap = new StackPane(avatar);
        avatarWrap.setPadding(new Insets(0, 0, 0, 16));
        avatarWrap.setAlignment(Pos.CENTER_LEFT);
        avatarWrap.setTranslateY(-30);

        headerBox.getChildren().addAll(banner, actionRow, avatarWrap, identity, stats, thinSeparator());
    }

    private Node followerStat() {
        VBox box = stat(String.valueOf(profile.getFollowerCount()), "Followers",
                () -> UserListPage.open(navigator, root, userId, true));
        followerCountLabel = (Label) box.getChildren().get(0);
        return box;
    }

    private VBox stat(String number, String label, Runnable onClick) {
        Label n = new Label(number);
        n.getStyleClass().add("stat-number");
        Label l = new Label(label);
        l.getStyleClass().add("text-muted");
        VBox box = new VBox(n, l);
        if (onClick != null) {
            box.setStyle("-fx-cursor: hand;");
            box.setOnMouseClicked(e -> onClick.run());
        }
        return box;
    }

    private Region thinSeparator() {
        Region r = new Region();
        r.setMinHeight(1);
        r.setStyle("-fx-background-color: -fx-border;");
        return r;
    }

    private void updateFollowButton() {
        followButton.setText(following ? "Following" : "Follow");
        followButton.getStyleClass().setAll(following ? "btn-outline" : "btn-primary");
    }

    private void toggleFollow() {
        boolean next = !following;
        following = next;
        updateFollowButton();
        int delta = next ? 1 : -1;
        profile.setFollowerCount(Math.max(0, profile.getFollowerCount() + delta));
        if (followerCountLabel != null) {
            followerCountLabel.setText(String.valueOf(profile.getFollowerCount()));
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("targetUserId", userId);
        PacketType type = next ? PacketType.FOLLOW : PacketType.UNFOLLOW;
        NetworkService.getInstance().sendRequest(
                Packet.request(type, null, payload),
                response -> {
                    if (!"OK".equals(response.getStatus())) {
                        following = !next;
                        updateFollowButton();
                    }
                });
    }

    private void onProfileUpdated(User updated) {
        this.profile = updated;
        if (updated.getId() == UserContext.getInstance().getUserId()) {
            UserContext.getInstance().setDisplayName(updated.getDisplayName());
            UserContext.getInstance().setAvatarUrl(updated.getAvatarUrl());
        }
        renderHeader();
        loadTweets();
    }
}
