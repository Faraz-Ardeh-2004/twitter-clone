package com.twitterclone.client.util;

import com.twitterclone.client.network.UserContext;
import javafx.scene.Scene;

/**
 * Applies the shared stylesheet to a Scene and toggles the dark theme by
 * adding/removing the "dark" style class on the scene root, based on the
 * user's current preference in {@link UserContext}.
 */
public final class Theme {

    private static final String STYLESHEET = "/css/app.css";

    private Theme() {
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }
        String css = Theme.class.getResource(STYLESHEET).toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
        refresh(scene);
    }

    /** Re-evaluates the dark class after a preference toggle. */
    public static void refresh(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        var classes = scene.getRoot().getStyleClass();
        classes.remove("dark");
        if (UserContext.getInstance().isDarkMode()) {
            classes.add("dark");
        }
    }
}
