package com.twitterclone.client.controller;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
import com.twitterclone.client.util.SceneNavigator;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 1 - Day 3-5
 * ============================================================
 * Controller for Login.fxml. The fx:id fields below must match that file exactly.
 *
 * NOTE: assumes AuthHandler's LOGIN response payload contains "userId"
 * (int) alongside the token - coordinate with Hesam if he settles on a
 * different shape.
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
            if ("OK".equals(response.getStatus())) {
                int userId = response.getPayload().get("userId").getAsInt();
                UserContext.getInstance().setSession(response.getToken(), userId, username);
                SceneNavigator.switchScene(loginButton, "/fxml/Dashboard.fxml");
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
