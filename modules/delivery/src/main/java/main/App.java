package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    public void start(Stage stage) throws IOException {
        String requested = System.getProperty("loop.start", "admin");
        String startView = "driver".equalsIgnoreCase(requested) ? "deliveryaccept" : "delivery";
        Parent root = loadFXML(startView);
        scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 900, 650);
        } else {
            scene.setRoot(root);
        }
        scene.getStylesheets().clear();
        stage.setScene(scene);
        stage.setTitle("LOOP - Delivery & Logistics");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxmlfiles/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
