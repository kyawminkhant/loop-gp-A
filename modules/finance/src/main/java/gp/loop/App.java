package gp.loop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import gp.loop.db.Database;
import java.io.IOException;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Database.initialize(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        String startView = System.getProperty("loop.start", "finance-dashboard");
        Parent root = loadFXML(startView);
        scene = new Scene(root, 1400, 900);
        /* Matches dashboard edge so no bright ring if the root is translucent. */
        scene.setFill(Color.web("#FFF8E1"));

        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Weekly Food Delivery - Finance & Reporting");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        if (scene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
