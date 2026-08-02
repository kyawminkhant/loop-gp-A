package utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationUtil {

    public static void switchScene(Stage stage, String fxmlPath, String cssPath) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root, 1000, 650);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().clear();
            scene.getStylesheets().add(NavigationUtil.class.getResource(cssPath).toExternalForm());

            Platform.runLater(() -> AnimationUtil.attachBackgroundAmbient(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
