package com.twitterclone.client.controller;

import com.google.gson.JsonObject;
import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.network.UserContext;
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

    /**
     * TODO(Faraz):
     *  1. Read the values of usernameField/passwordField.
     *  2. Basic validation: if either is empty, set statusLabel and return
     *     (don't send anything to the server).
     *  3. Build a JsonObject payload: {"username": ..., "password": ...}
     *  4. Packet request = Packet.request(PacketType.LOGIN, null, payload)
     *  5. Call NetworkService.getInstance().sendRequest(request, response -> { ... }).
     *  6. Inside the callback (already running on the JavaFX thread, no need
     *     to worry about Platform.runLater):
     *     - if response.getStatus() is "OK": populate
     *       UserContext.getInstance().setSession(token, userId, username)
     *       (read token/userId from response.getToken() and
     *       response.getPayload()) and switch the scene to Dashboard.fxml
     *       (see the FXMLLoader pattern in ClientMain).
     *     - if status is "ERROR": statusLabel.setText(response.getMessage())
     */
    @FXML
    private void onLoginButtonClick() {
        // TODO(Faraz): full implementation per the guide above.
    }

    /**
     * TODO(Faraz): swap the current Stage's scene for Register.fxml.
     * Hint: loginButton.getScene().getWindow() gives you the Stage; use the
     * same FXMLLoader pattern used in ClientMain.start.
     */
    @FXML
    private void onGoToRegisterClick() {
        // TODO(Faraz): full implementation per the guide above.
    }
}
