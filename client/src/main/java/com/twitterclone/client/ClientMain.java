package com.twitterclone.client;

import com.twitterclone.client.network.NetworkService;
import com.twitterclone.client.util.Theme;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ============================================================
 * Owner: Faraz (Frontend) | Phase 0 (skeleton) -> Phase 1 (real Login screen)
 * ============================================================
 * JavaFX application entry point. The first screen the user sees is Login.
 *
 * Establishes the network connection once for the whole lifetime of the app
 * and shows Login.fxml as the first screen.
 */
public class ClientMain extends Application {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 650;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            NetworkService.getInstance().connect(SERVER_HOST, SERVER_PORT);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Cannot reach the server at " + SERVER_HOST + ":" + SERVER_PORT
                            + ".\nStart the server first, then relaunch the client.");
            alert.setHeaderText("Connection failed");
            alert.showAndWait();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Twitter Clone");
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        Theme.apply(scene);
        primaryStage.setScene(scene);
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
