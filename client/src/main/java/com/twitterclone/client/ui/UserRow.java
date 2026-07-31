package com.twitterclone.client.ui;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.client.util.UiUtils;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * One user shown in a list (search results, followers/following, suggestions):
 * avatar, name/handle, bio snippet, and a follow/unfollow button. Clicking the
 * row opens that user's profile.
 */
public class UserRow extends HBox {

    private final User user;
    private final Navigator navigator;
    private boolean following;
    private Button followButton;

    public UserRow(User user, Navigator navigator) {
        this.user = user;
        this.navigator = navigator;
        this.following = user.isFollowing();

        setSpacing(10);
        setPadding(new Insets(12, 16, 12, 16));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("tweet-card");

        Node avatar = UiUtils.avatar(user.displayNameOrUsername(), user.getAvatarUrl(), 44);

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(user.displayNameOrUsername());
        name.getStyleClass().add("tweet-author");
        Label handle = new Label("@" + user.getUsername());
        handle.getStyleClass().add("tweet-handle");
        info.getChildren().addAll(name, handle);
        if (user.getBio() != null && !user.getBio().isBlank()) {
            Label bio = new Label(user.getBio());
            bio.getStyleClass().add("text-muted");
            bio.setWrapText(true);
            info.getChildren().add(bio);
        }

        getChildren().addAll(avatar, info);

        if (user.getId() != UserContext.getInstance().getUserId()) {
            followButton = new Button();
            updateFollowButton();
            followButton.setOnAction(e -> { e.consume(); toggleFollow(); });
            getChildren().add(followButton);
        }

        setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Button)) {
                navigator.openProfile(user.getId());
            }
        });
    }

    private void updateFollowButton() {
        followButton.setText(following ? "Following" : "Follow");
        followButton.getStyleClass().setAll(following ? "btn-outline" : "btn-primary");
    }

    private void toggleFollow() {
        boolean next = !following;
        following = next;
        updateFollowButton();
        JsonObject payload = new JsonObject();
        payload.addProperty("targetUserId", user.getId());
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
}
