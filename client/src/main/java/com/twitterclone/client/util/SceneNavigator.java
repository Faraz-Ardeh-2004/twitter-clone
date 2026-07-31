package com.twitterclone.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
// Theme is in the same package (com.twitterclone.client.util); no import needed.

/**
 * ============================================================
 * Owner: Faraz (Frontend)
 * ============================================================
 * Small shared helper so every controller doesn't repeat the same
 * "load an fxml and swap it into the current Stage" boilerplate.
 */
public final class SceneNavigator {

    private SceneNavigator() {
    }

    /** Replaces the scene of the Stage that {@code from} currently belongs to with the given fxml. */
    public static void switchScene(Node from, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) from.getScene().getWindow();
            Scene currentScene = from.getScene();
            Scene next = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            Theme.apply(next);
            stage.setScene(next);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fxml: " + fxmlPath, e);
        }
    }
}