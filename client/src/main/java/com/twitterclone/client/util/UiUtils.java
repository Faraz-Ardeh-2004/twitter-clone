package com.twitterclone.client.util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Small UI helpers shared across controllers: building avatar nodes, decoding
 * base64/data-URI images, and formatting relative timestamps.
 */
public final class UiUtils {

    private static final Color[] AVATAR_COLORS = {
            Color.web("#1d9bf0"), Color.web("#794bc4"), Color.web("#f91880"),
            Color.web("#00ba7c"), Color.web("#ff7a00"), Color.web("#e0245e")
    };

    private UiUtils() {
    }

    /** Builds a circular avatar: the user's image if available, else a colored initial. */
    public static Node avatar(String displayName, String imageData, double size) {
        Circle circle = new Circle(size / 2);
        Image img = decodeImage(imageData);
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);

        if (img != null) {
            circle.setFill(new ImagePattern(img));
            pane.getChildren().add(circle);
        } else {
            String initials = initials(displayName);
            circle.setFill(colorFor(displayName));
            Label label = new Label(initials);
            label.getStyleClass().add("avatar-label");
            label.setStyle("-fx-font-size: " + (size / 2.4) + "px;");
            pane.getChildren().addAll(circle, label);
        }
        return pane;
    }

    /** Decodes a data URI, raw base64, or http(s) URL into an Image; null if not decodable. */
    public static Image decodeImage(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }
        try {
            if (data.startsWith("http://") || data.startsWith("https://")) {
                return new Image(data, false);
            }
            String base64 = data;
            int comma = data.indexOf(',');
            if (data.startsWith("data:") && comma >= 0) {
                base64 = data.substring(comma + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(base64.replaceAll("\\s", ""));
            Image image = new Image(new ByteArrayInputStream(bytes));
            return image.isError() ? null : image;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private static Color colorFor(String key) {
        int h = (key == null) ? 0 : Math.abs(key.hashCode());
        return AVATAR_COLORS[h % AVATAR_COLORS.length];
    }

    /** Formats an ISO-8601 local timestamp as a compact relative time (e.g. "3m", "2h", "Jul 30"). */
    public static String relativeTime(String isoCreatedAt) {
        if (isoCreatedAt == null) {
            return "";
        }
        try {
            LocalDateTime t = LocalDateTime.parse(isoCreatedAt);
            Duration d = Duration.between(t, LocalDateTime.now());
            long secs = d.getSeconds();
            if (secs < 0) secs = 0;
            if (secs < 60) return secs + "s";
            long mins = secs / 60;
            if (mins < 60) return mins + "m";
            long hours = mins / 60;
            if (hours < 24) return hours + "h";
            long days = hours / 24;
            if (days < 7) return days + "d";
            return t.format(DateTimeFormatter.ofPattern("MMM d"));
        } catch (RuntimeException e) {
            return isoCreatedAt;
        }
    }
}
