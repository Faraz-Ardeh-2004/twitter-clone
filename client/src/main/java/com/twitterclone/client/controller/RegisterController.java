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
 * Controller for Register.fxml. Structure is nearly identical to LoginController.
 *
 * Team decision: after a successful REGISTER, immediately fire a LOGIN with
 * the same credentials so the user lands straight on the Dashboard instead
 * of being sent back to the Login screen.
 */
public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button goToLoginButton;

    @FXML
    private void onRegisterButtonClick() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank()
                || email == null || !email.contains("@")
                || password == null || password.length() < 6) {
            statusLabel.setText("Fill in a username, a valid email, and a password of 6+ characters.");
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("email", email);
        payload.addProperty("password", password);

        registerButton.setDisable(true);
        NetworkService.getInstance().sendRequest(Packet.request(PacketType.REGISTER, null, payload), response -> {
            if (!"OK".equals(response.getStatus())) {
                registerButton.setDisable(false);
                statusLabel.setText(response.getMessage() != null ? response.getMessage() : "Registration failed.");
                return;
            }
            loginAfterRegister(username, password);
        });
    }

    private void loginAfterRegister(String username, String password) {
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);

        NetworkService.getInstance().sendRequest(Packet.request(PacketType.LOGIN, null, payload), response -> {
            registerButton.setDisable(false);
            if ("OK".equals(response.getStatus()) && response.getPayload() != null) {
                JsonObject p = response.getPayload();
                int userId = p.get("userId").getAsInt();
                String displayName = p.has("displayName") ? p.get("displayName").getAsString() : username;
                String avatarUrl = p.has("avatarUrl") ? p.get("avatarUrl").getAsString() : null;
                UserContext.getInstance().setSession(response.getToken(), userId, username, displayName, avatarUrl);
                new AppShell((Stage) registerButton.getScene().getWindow());
            } else {
                // Registered fine but the follow-up auto-login failed; send them to Login manually.
                SceneNavigator.switchScene(registerButton, "/fxml/Login.fxml");
            }
        });
    }

    @FXML
    private void onGoToLoginClick() {
        SceneNavigator.switchScene(goToLoginButton, "/fxml/Login.fxml");
    }
}
