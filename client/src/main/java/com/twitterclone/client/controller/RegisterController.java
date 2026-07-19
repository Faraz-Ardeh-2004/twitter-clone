package com.twitterclone.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 1 - Day 3-5
 * ============================================================
 * Controller for Register.fxml. Structure is nearly identical to LoginController.
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

    /**
     * TODO(Faraz):
     *  1. Basic validation (fields not empty, simple email format, password length).
     *  2. payload = {"username":..., "email":..., "password":...}
     *  3. Send Packet.request(PacketType.REGISTER, null, payload).
     *  4. In the callback: if OK -> route the user to Login.fxml (or log them
     *     in directly - team decision; the spec only says the user should be
     *     routed to the main page after registering, so it's likely you'll
     *     automatically fire a LOGIN too after a successful REGISTER).
     *     If ERROR (e.g. duplicate username) -> populate statusLabel.
     */
    @FXML
    private void onRegisterButtonClick() {
        // TODO(Faraz): full implementation per the guide above.
    }

    /** TODO(Faraz): return the scene to Login.fxml (same FXMLLoader pattern). */
    @FXML
    private void onGoToLoginClick() {
        // TODO(Faraz): full implementation per the guide above.
    }
}
