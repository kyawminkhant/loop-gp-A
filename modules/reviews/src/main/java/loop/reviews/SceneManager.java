package loop.reviews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Central navigation controller. Holds a single Stage and a single Scene, and
 * swaps the Scene's ROOT node when moving between screens - this reproduces the
 * screen-switching behaviour of the original web prototype.
 *
 * Every screen load is wrapped in a try/catch that PRINTS the stack trace
 * (never silently swallows it) and surfaces an on-screen alert (STEP 3).
 */
public final class SceneManager {

    private static Stage stage;
    private static Scene scene;

    private SceneManager() { }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        scene = primaryStage.getScene();
    }

    public static Stage getStage() {
        return stage;
    }

    /**
     * Load the FXML at /fxml/{name}.fxml and set it as the scene root.
     * @param name file name without path or extension, e.g. "home".
     */
    public static void switchTo(String name) {
        try {
            String path = "/fxml/" + name + ".fxml";
            URL url = SceneManager.class.getResource(path);
            if (url == null) {
                throw new IllegalStateException("FXML not found on classpath: " + path);
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            if (scene == null) {
                scene = new Scene(root, 1180, 760);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            URL css = SceneManager.class.getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().setAll(css.toExternalForm());
            } else {
                scene.getStylesheets().clear();
                System.err.println("[WARN] style.css not found on classpath.");
            }
        } catch (Exception e) {
            // Do NOT swallow - print and show.
            System.err.println("[SceneManager] Failed to load screen '" + name + "':");
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation error");
            alert.setHeaderText("Could not open screen: " + name);
            alert.setContentText(String.valueOf(e.getMessage()));
            alert.showAndWait();
        }
    }
}
