package com.twitterclone.client.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.util.UiUtils;
import com.twitterclone.shared.model.Notification;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The notifications timeline: follows, likes, replies, and retweets aimed at the
 * current user. Opening the page marks everything as read (clearing the nav
 * badge). Each row links to the relevant tweet or the actor's profile.
 */
public class NotificationsPage {

    private final Gson gson = new Gson();
    private final Navigator navigator;
    private final VBox list = new VBox();
    private final VBox root = new VBox();

    public NotificationsPage(Navigator navigator) {
        this.navigator = navigator;

        Label title = new Label("Notifications");
        title.getStyleClass().add("title");
        HBox header = new HBox(title);
        header.setPadding(new Insets(14, 16, 14, 16));

        ScrollPane scroll = new ScrollPane(list);
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
        payload.addProperty("limit", 50);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_NOTIFICATIONS, null, payload),
                response -> {
                    list.getChildren().clear();
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        Notification[] notifs = gson.fromJson(
                                response.getPayload().get("notifications"), Notification[].class);
                        if (notifs.length == 0) {
                            Label empty = new Label("Nothing here yet.");
                            empty.getStyleClass().add("text-muted");
                            empty.setPadding(new Insets(24));
                            list.getChildren().add(empty);
                        }
                        for (Notification n : notifs) {
                            list.getChildren().add(row(n));
                        }
                        markRead();
                    }
                });
    }

    private Node row(Notification n) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("notif-row");
        if (!n.isRead()) {
            box.getStyleClass().add("notif-unread");
        }

        Node typeIcon = Icons.create(iconFor(n.getType()), 22, colorFor(n.getType()));
        Node avatar = UiUtils.avatar(n.getActorUsername(), null, 40);

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label main = new Label(n.describe());
        main.getStyleClass().add("label");
        main.setWrapText(true);
        Label time = new Label(UiUtils.relativeTime(n.getCreatedAt()));
        time.getStyleClass().add("tweet-time");
        text.getChildren().addAll(main, time);

        box.getChildren().addAll(typeIcon, avatar, text);

        box.setStyle("-fx-cursor: hand;");
        box.setOnMouseClicked(e -> {
            if (n.getTweetId() != null) {
                navigator.openTweetById(n.getTweetId());
            } else {
                navigator.openProfile(n.getActorId());
            }
        });
        return box;
    }

    private String iconFor(String type) {
        if (type == null) return "bell";
        return switch (type) {
            case "FOLLOW" -> "user-plus";
            case "LIKE" -> "heart";
            case "REPLY" -> "message-circle";
            case "RETWEET" -> "repeat";
            default -> "bell";
        };
    }

    private String colorFor(String type) {
        if (type == null) return "icon-muted";
        return switch (type) {
            case "LIKE" -> "icon-like";
            case "RETWEET" -> "icon-retweet";
            case "FOLLOW", "REPLY" -> "icon-accent";
            default -> "icon-muted";
        };
    }

    private void markRead() {
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.MARK_NOTIFICATIONS_READ, null, new JsonObject()),
                response -> { /* badge cleared by AppShell */ });
    }
}
