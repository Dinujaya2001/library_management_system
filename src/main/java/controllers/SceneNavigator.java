package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {

    private SceneNavigator() {
    }

    public static void switchScene(Stage stage, String fxmlFile, String title) throws IOException {
        URL fxmlUrl = SceneNavigator.class.getResource("/view/" + fxmlFile);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found: /view/" + fxmlFile);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        URL cssUrl = SceneNavigator.class.getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle(title);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}