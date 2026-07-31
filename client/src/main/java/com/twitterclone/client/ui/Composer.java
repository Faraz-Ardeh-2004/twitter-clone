package com.twitterclone.client.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.util.UiUtils;
import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import com.twitterclone.shared.protocol.Protocol;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tweet/reply composer: a text area with a live character counter (enforcing
 * the shared 280 limit), image attachments (up to 4, shown as removable
 * thumbnails), and a post button. On success it clears itself and hands the
 * created Tweet to the supplied callback.
 */
public class Composer extends VBox {

    private final Integer parentTweetId;
    private final Consumer<Tweet> onPosted;

    private final TextArea textArea = new TextArea();
    private final Label counter = new Label(String.valueOf(Protocol.MAX_TWEET_LENGTH));
    private final Button postButton;
    private final FlowPane thumbs = new FlowPane(8, 8);
    private final List<String> media = new ArrayList<>();

    public Composer(Integer parentTweetId, String buttonLabel, String prompt, Consumer<Tweet> onPosted) {
        this.parentTweetId = parentTweetId;
        this.onPosted = onPosted;

        setSpacing(8);
        setPadding(new Insets(12, 16, 12, 16));
        getStyleClass().add("composer");

        textArea.setPromptText(prompt);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(3);

        postButton = new Button(buttonLabel);
        postButton.getStyleClass().add("btn-primary");
        postButton.setDisable(true);
        postButton.setOnAction(e -> submit());

        Button attach = new Button("🖼 Image");
        attach.getStyleClass().add("btn-outline");
        attach.setOnAction(e -> attachImage());

        counter.getStyleClass().add("text-muted");

        textArea.textProperty().addListener((obs, o, n) -> updateState());

        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(attach, spacer, counter, postButton);

        getChildren().addAll(textArea, thumbs, bar);
        updateState();
    }

    private void updateState() {
        int len = textArea.getText() == null ? 0 : textArea.getText().length();
        int remaining = Protocol.MAX_TWEET_LENGTH - len;
        counter.setText(String.valueOf(remaining));
        counter.setStyle(remaining < 0 ? "-fx-text-fill: #f4212e;" : "");
        boolean hasText = len > 0 && remaining >= 0;
        postButton.setDisable(!(hasText || !media.isEmpty()) || remaining < 0);
    }

    private void attachImage() {
        if (media.size() >= Protocol.MAX_MEDIA_PER_TWEET) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Attach image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        var file = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String mime = probeMime(file.getName());
            String dataUri = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
            if (dataUri.length() > Protocol.MAX_MEDIA_BASE64_LENGTH) {
                showTooLarge();
                return;
            }
            media.add(dataUri);
            addThumb(dataUri);
            updateState();
        } catch (IOException e) {
            // ignore unreadable file
        }
    }

    private void addThumb(String dataUri) {
        Image img = UiUtils.decodeImage(dataUri);
        if (img == null) {
            return;
        }
        ImageView view = new ImageView(img);
        view.setFitWidth(72);
        view.setFitHeight(72);
        view.setPreserveRatio(true);
        Button remove = new Button("✕");
        remove.getStyleClass().add("action-button");
        StackPane cell = new StackPane(view, remove);
        StackPane.setAlignment(remove, Pos.TOP_RIGHT);
        remove.setOnAction(e -> {
            media.remove(dataUri);
            thumbs.getChildren().remove(cell);
            updateState();
        });
        thumbs.getChildren().add(cell);
    }

    private void submit() {
        String content = textArea.getText() == null ? "" : textArea.getText().trim();
        if (content.isEmpty() && media.isEmpty()) {
            return;
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("content", content);
        if (parentTweetId != null) {
            payload.addProperty("parentTweetId", parentTweetId);
        }
        JsonArray arr = new JsonArray();
        media.forEach(arr::add);
        payload.add("media", arr);

        postButton.setDisable(true);
        NetworkService.getInstance().sendRequest(
                Packet.request(PacketType.CREATE_TWEET, null, payload),
                response -> {
                    postButton.setDisable(false);
                    if ("OK".equals(response.getStatus())) {
                        textArea.clear();
                        media.clear();
                        thumbs.getChildren().clear();
                        updateState();
                        if (onPosted != null && response.getPayload() != null) {
                            Tweet t = new com.google.gson.Gson()
                                    .fromJson(response.getPayload().get("tweet"), Tweet.class);
                            onPosted.accept(t);
                        }
                    } else {
                        counter.setText(response.getMessage() != null ? response.getMessage() : "Failed");
                        counter.setStyle("-fx-text-fill: #f4212e;");
                    }
                });
    }

    private void showTooLarge() {
        counter.setText("Image too large");
        counter.setStyle("-fx-text-fill: #f4212e;");
    }

    private static String probeMime(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }
}
