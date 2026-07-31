package com.twitterclone.client.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * A list of users — either the followers or the following of a given user.
 * Opened as a lightweight modal window from the profile stats.
 */
public class UserListPage {

    private final Gson gson = new Gson();
    private final Navigator navigator;
    private final int userId;
    private final boolean followers;
    private final VBox list = new VBox();

    private UserListPage(Navigator navigator, int userId, boolean followers) {
        this.navigator = navigator;
        this.userId = userId;
        this.followers = followers;
    }

    /** Opens the followers/following list in a modal window over the given node's window. */
    public static void open(Navigator navigator, Node ownerNode, int userId, boolean followers) {
        UserListPage page = new UserListPage(navigator, userId, followers);
        Window owner = ownerNode.getScene() == null ? null : ownerNode.getScene().getWindow();
        page.showWindow(owner);
    }

    private void showWindow(Window owner) {
        Label title = new Label(followers ? "Followers" : "Following");
        title.getStyleClass().add("title");
        HBox header = new HBox(title);
        header.setPadding(new Insets(14, 16, 14, 16));

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox rootBox = new VBox(header, scroll);
        rootBox.getStyleClass().add("root");

        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setTitle(followers ? "Followers" : "Following");
        Scene scene = new Scene(rootBox, 420, 560);
        com.twitterclone.client.util.Theme.apply(scene);
        stage.setScene(scene);
        stage.show();

        // Close the modal when a profile is opened from within it, so navigation
        // happens in the main window.
        load(stage);
    }

    private void load(Stage stage) {
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", userId);
        PacketType type = followers ? PacketType.GET_FOLLOWERS : PacketType.GET_FOLLOWING;
        NetworkService.getInstance().sendRequest(
                Packet.request(type, null, payload),
                response -> {
                    list.getChildren().clear();
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        User[] users = gson.fromJson(response.getPayload().get("users"), User[].class);
                        if (users.length == 0) {
                            Label empty = new Label("No users to show.");
                            empty.getStyleClass().add("text-muted");
                            empty.setPadding(new Insets(20));
                            list.getChildren().add(empty);
                        }
                        for (User u : users) {
                            UserRow row = new UserRow(u, wrapNavigator(stage));
                            list.getChildren().add(row);
                        }
                    }
                });
    }

    /** Wraps the shared navigator so opening a profile also closes this window. */
    private Navigator wrapNavigator(Stage stage) {
        return new Navigator() {
            public void openHome() { stage.close(); navigator.openHome(); }
            public void openProfile(int id) { stage.close(); navigator.openProfile(id); }
            public void openProfileByUsername(String u) { stage.close(); navigator.openProfileByUsername(u); }
            public void openTweetDetail(com.twitterclone.shared.model.Tweet t) { stage.close(); navigator.openTweetDetail(t); }
            public void openTweetById(int id) { stage.close(); navigator.openTweetById(id); }
            public void openHashtag(String tag) { stage.close(); navigator.openHashtag(tag); }
            public void openSearch(String q) { stage.close(); navigator.openSearch(q); }
            public void openNotifications() { stage.close(); navigator.openNotifications(); }
        };
    }
}
