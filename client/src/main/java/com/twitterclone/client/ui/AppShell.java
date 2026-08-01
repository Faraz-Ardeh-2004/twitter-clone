package com.twitterclone.client.ui;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.client.util.Theme;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The main authenticated window and the app's {@link Navigator}. A persistent
 * left navigation column swaps the center content between Home, Search,
 * Notifications, and Profile pages. Also owns the dark-mode toggle, logout, and
 * the live notification badge (driven by NOTIFICATION_PUSH).
 */
public class AppShell implements Navigator {

    private final Stage stage;
    private final BorderPane rootPane = new BorderPane();
    private final StackPane center = new StackPane();
    private final HomePage home;

    private Button notificationsButton;
    private Button darkModeButton;
    private int unread = 0;

    public AppShell(Stage stage) {
        this.stage = stage;
        this.home = new HomePage(this);

        rootPane.getStyleClass().add("root");
        rootPane.setLeft(buildNav());
        rootPane.setCenter(center);

        Scene scene = new Scene(rootPane, 1000, 720);
        Theme.apply(scene);
        stage.setScene(scene);
        stage.setTitle("Twitter Clone — @" + UserContext.getInstance().getUsername());

        // Live notification badge.
        NetworkService.getInstance().addPushListener(PacketType.NOTIFICATION_PUSH.name(), p -> bumpUnread());
        loadUnreadCount();

        openHome();
    }

    private Node buildNav() {
        VBox nav = new VBox(6);
        nav.getStyleClass().add("nav");
        nav.setPadding(new Insets(16, 12, 16, 12));
        nav.setPrefWidth(230);

        Label logo = new Label("Twitter Clone");
        logo.getStyleClass().add("title");
        logo.setGraphic(Icons.create("bird", 24, "icon-accent"));
        logo.setGraphicTextGap(8);
        logo.setPadding(new Insets(0, 0, 12, 8));

        Button homeBtn = navButton("Home", "house", this::openHome);
        Button searchBtn = navButton("Search", "search", () -> openSearch(""));
        notificationsButton = navButton("Notifications", "bell", this::openNotifications);
        Button profileBtn = navButton("Profile", "user",
                () -> openProfile(UserContext.getInstance().getUserId()));

        Button tweetBtn = new Button("Tweet");
        tweetBtn.getStyleClass().add("btn-primary");
        tweetBtn.setGraphic(Icons.create("send", 16, "icon-white"));
        tweetBtn.setGraphicTextGap(8);
        tweetBtn.setMaxWidth(Double.MAX_VALUE);
        tweetBtn.setOnAction(e -> openHome());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        darkModeButton = navButton(darkText(), darkIcon(), this::toggleDark);
        Button logoutBtn = navButton("Logout", "log-out", this::logout);

        nav.getChildren().addAll(logo, homeBtn, searchBtn, notificationsButton, profileBtn,
                tweetBtn, spacer, darkModeButton, logoutBtn);
        return nav;
    }

    private Button navButton(String text, String iconName, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        b.setGraphic(Icons.create(iconName, 20, "icon-default"));
        b.setGraphicTextGap(14);
        b.setContentDisplay(ContentDisplay.LEFT);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setOnAction(e -> action.run());
        return b;
    }

    private void setContent(Node node) {
        center.getChildren().setAll(node);
    }

    // ---- Navigator ----

    @Override
    public void openHome() {
        home.load();
        setContent(home.getView());
    }

    @Override
    public void openProfile(int userId) {
        setContent(new ProfilePage(this, userId).getView());
    }

    @Override
    public void openProfileByUsername(String username) {
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_PROFILE, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                        User u = new com.google.gson.Gson()
                                .fromJson(response.getPayload().get("user"), User.class);
                        openProfile(u.getId());
                    }
                });
    }

    @Override
    public void openTweetDetail(com.twitterclone.shared.model.Tweet tweet) {
        setContent(new TweetDetailPage(this, tweet.getId()).getView());
    }

    @Override
    public void openTweetById(int tweetId) {
        setContent(new TweetDetailPage(this, tweetId).getView());
    }

    @Override
    public void openHashtag(String tag) {
        setContent(new SearchPage(this, "#" + tag).getView());
    }

    @Override
    public void openSearch(String query) {
        setContent(new SearchPage(this, query).getView());
    }

    @Override
    public void openNotifications() {
        unread = 0;
        refreshNotificationsLabel();
        setContent(new NotificationsPage(this).getView());
    }

    // ---- notification badge ----

    private void bumpUnread() {
        unread++;
        refreshNotificationsLabel();
    }

    private void loadUnreadCount() {
        JsonObject payload = new JsonObject();
        payload.addProperty("limit", 1);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.GET_NOTIFICATIONS, null, payload),
                response -> {
                    if ("OK".equals(response.getStatus()) && response.getPayload() != null
                            && response.getPayload().has("unread")) {
                        unread = response.getPayload().get("unread").getAsInt();
                        refreshNotificationsLabel();
                    }
                });
    }

    private void refreshNotificationsLabel() {
        notificationsButton.setText(unread > 0 ? "Notifications (" + unread + ")" : "Notifications");
    }

    // ---- dark mode & logout ----

    private void toggleDark() {
        UserContext.getInstance().toggleDarkMode();
        Theme.refresh(stage.getScene());
        darkModeButton.setText(darkText());
        darkModeButton.setGraphic(Icons.create(darkIcon(), 20, "icon-default"));
    }

    private String darkText() {
        return UserContext.getInstance().isDarkMode() ? "Light mode" : "Dark mode";
    }

    private String darkIcon() {
        return UserContext.getInstance().isDarkMode() ? "sun" : "moon";
    }

    private void logout() {
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.LOGOUT, null, new JsonObject()), r -> { });
        NetworkService.getInstance().clearListeners();
        UserContext.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginRoot = loader.load();
            Scene scene = new Scene(loginRoot, 1000, 720);
            Theme.apply(scene);
            stage.setScene(scene);
        } catch (Exception e) {
            throw new RuntimeException("Failed to return to login", e);
        }
    }
}
