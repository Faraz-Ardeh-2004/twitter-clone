package com.twitterclone.client.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.util.Theme;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.nio.file.Files;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * Modal dialog to edit the current user's profile: display name, bio, and
 * avatar/banner images. Sends UPDATE_PROFILE and returns the refreshed User.
 */
public final class EditProfileDialog {

    private EditProfileDialog() {
    }

    public static void show(Window owner, User current, Consumer<User> onSaved) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setTitle("Edit profile");

        TextField displayName = new TextField(current.getDisplayName());
        displayName.setPromptText("Display name");
        TextArea bio = new TextArea(current.getBio());
        bio.setPromptText("Bio");
        bio.setWrapText(true);
        bio.setPrefRowCount(3);

        String[] avatar = { current.getAvatarUrl() };
        String[] banner = { current.getBannerUrl() };

        Label avatarStatus = new Label(avatar[0] != null ? "Avatar set" : "No avatar");
        avatarStatus.getStyleClass().add("text-muted");
        Button avatarBtn = new Button("Choose avatar");
        avatarBtn.getStyleClass().add("btn-outline");
        avatarBtn.setOnAction(e -> {
            String data = pickImage(stage);
            if (data != null) { avatar[0] = data; avatarStatus.setText("Avatar updated"); }
        });

        Label bannerStatus = new Label(banner[0] != null ? "Banner set" : "No banner");
        bannerStatus.getStyleClass().add("text-muted");
        Button bannerBtn = new Button("Choose banner");
        bannerBtn.getStyleClass().add("btn-outline");
        bannerBtn.setOnAction(e -> {
            String data = pickImage(stage);
            if (data != null) { banner[0] = data; bannerStatus.setText("Banner updated"); }
        });

        Label error = new Label();
        error.getStyleClass().add("error-label");

        Button save = new Button("Save");
        save.getStyleClass().add("btn-primary");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("btn-outline");
        cancel.setOnAction(e -> stage.close());

        save.setOnAction(e -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("displayName", displayName.getText() == null ? "" : displayName.getText().trim());
            payload.addProperty("bio", bio.getText() == null ? "" : bio.getText().trim());
            if (avatar[0] != null) payload.addProperty("avatarUrl", avatar[0]);
            if (banner[0] != null) payload.addProperty("bannerUrl", banner[0]);
            save.setDisable(true);
            NetworkService.getInstance().sendRequest(
                    Packet.request(PacketType.UPDATE_PROFILE, null, payload),
                    response -> {
                        save.setDisable(false);
                        if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                            User updated = new Gson().fromJson(response.getPayload().get("user"), User.class);
                            if (onSaved != null) onSaved.accept(updated);
                            stage.close();
                        } else {
                            error.setText(response.getMessage() != null ? response.getMessage() : "Update failed");
                        }
                    });
        });

        VBox box = new VBox(12,
                new Label("Display name"), displayName,
                new Label("Bio"), bio,
                new HBox(10, avatarBtn, avatarStatus),
                new HBox(10, bannerBtn, bannerStatus),
                error,
                new HBox(10, save, cancel));
        box.setPadding(new Insets(20));
        box.getStyleClass().add("root");

        Scene scene = new Scene(box, 420, 460);
        Theme.apply(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static String pickImage(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        var file = chooser.showOpenDialog(owner);
        if (file == null) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String lower = file.getName().toLowerCase();
            String mime = lower.endsWith(".png") ? "image/png"
                    : lower.endsWith(".gif") ? "image/gif" : "image/jpeg";
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
