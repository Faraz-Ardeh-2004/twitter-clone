package com.twitterclone.client;

import com.twitterclone.client.network.NetworkService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 0 (skeleton) -> Phase 1 (real Login screen)
 * ============================================================
 * JavaFX application entry point. The first screen the user sees is Login.
 *
 * TODO(Faraz - Phase 0): for the initial test (before Login.fxml is ready),
 * you can temporarily use a simple scene with a Ping button and a Label
 * (per the Phase 0 DoD), and once that test passes, point this start method
 * at Login.fxml instead.
 *
 * TODO(Faraz - Phase 1): establish the network connection here (once, for
 * the whole lifetime of the app):
 *   NetworkService.getInstance().connect("localhost", 8080);
 * and close it in stop().
 */
public class ClientMain extends Application {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 650;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // TODO(Faraz): make the server host/port configurable (or at least
        // keep it as a single constant defined in one place).
        NetworkService.getInstance().connect("localhost", 8080);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Twitter Clone");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // TODO(Faraz): close the network connection cleanly so the
        // background listener thread also stops.
        NetworkService.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
