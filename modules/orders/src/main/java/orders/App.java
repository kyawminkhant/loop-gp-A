package orders;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Orders.fxml"));
        Parent root = loader.load();

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1200, 780);
        } else {
            scene.setRoot(root);
        }
        scene.getStylesheets().setAll(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("Loop - Orders");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
