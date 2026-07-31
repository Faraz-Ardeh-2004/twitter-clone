package com.twitterclone.client.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Arrays;

/**
 * Search across people, tweets, and hashtags. A query beginning with '#' is
 * treated as a hashtag search; otherwise both users and tweet text are searched
 * and shown under "People" and "Tweets" headings. Also shows trending hashtags
 * when the query is empty.
 */
public class SearchPage {

    private final Gson gson = new Gson();
    private final Navigator navigator;
    private final TextField searchField = new TextField();
    private final VBox results = new VBox(8);
    private final VBox root = new VBox();

    public SearchPage(Navigator navigator, String initialQuery) {
        this.navigator = navigator;

        searchField.setPromptText("Search people, tweets, or #hashtags");
        searchField.setText(initialQuery == null ? "" : initialQuery);
        Button go = new Button("Search");
        go.getStyleClass().add("btn-primary");
        go.setOnAction(e -> runSearch());
        searchField.setOnAction(e -> runSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox bar = new HBox(10, searchField, go);
        bar.setPadding(new Insets(14, 16, 8, 16));

        ScrollPane scroll = new ScrollPane(results);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(bar, scroll);

        if (initialQuery != null && !initialQuery.isBlank()) {
            runSearch();
        } else {
            loadTrending();
        }
    }

    public Node getView() {
        return root;
    }

    private void runSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            loadTrending();
            return;
        }
        results.getChildren().clear();
        if (query.startsWith("#")) {
            hashtagSearch(query.substring(1));
            return;
        }
        userSearch(query);
        tweetSearch(query);
    }

    private void userSearch(String query) {
        JsonObject payload = new JsonObject();
        payload.addProperty("query", query);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.SEARCH_USERS, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        User[] users = gson.fromJson(response.getPayload().get("users"), User[].class);
                        if (users.length > 0) {
                            results.getChildren().add(0, heading("People"));
                            int idx = 1;
                            for (User u : users) {
                                results.getChildren().add(idx++, new UserRow(u, navigator));
                            }
                        }
                    }
                });
    }

    private void tweetSearch(String query) {
        JsonObject payload = new JsonObject();
        payload.addProperty("query", query);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.SEARCH_TWEETS, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        Tweet[] tweets = gson.fromJson(response.getPayload().get("tweets"), Tweet[].class);
                        results.getChildren().add(heading("Tweets"));
                        FeedList feed = new FeedList(navigator);
                        feed.setEmptyText("No matching tweets.");
                        feed.render(Arrays.asList(tweets));
                        results.getChildren().add(feed);
                    }
                });
    }

    private void hashtagSearch(String tag) {
        JsonObject payload = new JsonObject();
        payload.addProperty("query", tag);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.SEARCH_HASHTAG, null, payload),
                response -> {
                    results.getChildren().clear();
                    results.getChildren().add(heading("#" + tag));
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        Tweet[] tweets = gson.fromJson(response.getPayload().get("tweets"), Tweet[].class);
                        FeedList feed = new FeedList(navigator);
                        feed.setEmptyText("No tweets with #" + tag + " yet.");
                        feed.render(Arrays.asList(tweets));
                        results.getChildren().add(feed);
                    }
                });
    }

    private void loadTrending() {
        results.getChildren().clear();
        results.getChildren().add(heading("Trending hashtags"));
        JsonObject payload = new JsonObject();
        payload.addProperty("limit", 10);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.TRENDING_HASHTAGS, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        var arr = response.getPayload().getAsJsonArray("hashtags");
                        if (arr.size() == 0) {
                            Label empty = new Label("No trends yet — start tweeting with #hashtags!");
                            empty.getStyleClass().add("text-muted");
                            empty.setPadding(new Insets(16));
                            results.getChildren().add(empty);
                        }
                        for (var el : arr) {
                            JsonObject o = el.getAsJsonObject();
                            String tag = o.get("tag").getAsString();
                            int count = o.get("count").getAsInt();
                            results.getChildren().add(trendRow(tag, count));
                        }
                    }
                });
    }

    private Node trendRow(String tag, int count) {
        Label label = new Label("#" + tag);
        label.getStyleClass().add("hashtag");
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label sub = new Label(count + (count == 1 ? " tweet" : " tweets"));
        sub.getStyleClass().add("text-muted");
        VBox box = new VBox(2, label, sub);
        box.setPadding(new Insets(10, 16, 10, 16));
        box.getStyleClass().add("tweet-card");
        box.setStyle("-fx-cursor: hand;");
        box.setOnMouseClicked(e -> navigator.openHashtag(tag));
        return box;
    }

    private Label heading(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("h2");
        l.setPadding(new Insets(10, 16, 4, 16));
        return l;
    }
}
