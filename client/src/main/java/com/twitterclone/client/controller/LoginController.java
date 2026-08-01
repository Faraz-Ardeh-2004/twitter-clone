package com.twitterclone.client.controller;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.client.ui.AppShell;
import com.twitterclone.client.util.SceneNavigator;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 1 - Day 3-5
 * ============================================================
 * Controller for Login.fxml. The fx:id fields below must match that file exactly.
 *
 * NOTE: assumes AuthHandler's LOGIN response payload contains "userId"
 * (int) alongside the token - coordinate with Hesam if he settles on a
 * different shape.
 *
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button goToRegisterButton;

    @FXML
    private Label brandLabel;

    @FXML
    public void initialize() {
        brandLabel.setGraphic(com.twitterclone.client.ui.Icons.create("bird", 26, "icon-accent"));
        brandLabel.setGraphicTextGap(8);
    }

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            statusLabel.setText("Username and password are required.");
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);

        loginButton.setDisable(true);
        NetworkService.getInstance().sendRequest(Packet.request(PacketType.LOGIN, null, payload), response -> {
            loginButton.setDisable(false);
            if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                JsonObject p = response.getPayload();
                int userId = p.get("userId").getAsInt();
                String displayName = p.has("displayName") ? p.get("displayName").getAsString() : username;
                String avatarUrl = p.has("avatarUrl") ? p.get("avatarUrl").getAsString() : null;
                UserContext.getInstance().setSession(response.getToken(), userId, username, displayName, avatarUrl);
                new AppShell((Stage) loginButton.getScene().getWindow());
            } else {
                statusLabel.setText(response.getMessage() != null ? response.getMessage() : "Login failed.");
            }
        });
    }

    @FXML
    private void onGoToRegisterClick() {
        SceneNavigator.switchScene(goToRegisterButton, "/fxml/Register.fxml");
    }
}
