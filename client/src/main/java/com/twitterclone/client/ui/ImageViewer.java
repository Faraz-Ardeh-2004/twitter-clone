package com.twitterclone.client.ui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Opens an image at (up to) full size in its own window. Used for the
 * "image full-size viewing" bonus when a media thumbnail is clicked.
 */
public final class ImageViewer {

    private ImageViewer() {
    }

    public static void show(Image image) {
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        double maxW = Math.min(image.getWidth(), 1000);
        view.setFitWidth(maxW);

        StackPane root = new StackPane(view);
        root.setStyle("-fx-background-color: black;");
        Stage stage = new Stage();
        stage.setTitle("Image");
        Scene scene = new Scene(root, Math.max(320, maxW), Math.max(240, Math.min(image.getHeight(), 800)),
                Color.BLACK);
        scene.setOnMouseClicked(e -> stage.close());
        stage.setScene(scene);
        stage.show();
    }
}
