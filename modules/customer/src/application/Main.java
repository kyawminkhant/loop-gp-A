package application;

import database.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.AnimationUtil;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseConnection.initializeDatabase();

            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1000, 650);
            } else {
                scene.setRoot(root);
            }
            scene.getStylesheets().setAll(getClass().getResource("/styles/app.css").toExternalForm());

            primaryStage.getIcons().addAll(
                    new Image(getClass().getResourceAsStream("/images/app_icon_16.png")),
                    new Image(getClass().getResourceAsStream("/images/app_icon_32.png")),
                    new Image(getClass().getResourceAsStream("/images/app_icon_64.png")),
                    new Image(getClass().getResourceAsStream("/images/app_icon_128.png")),
                    new Image(getClass().getResourceAsStream("/images/app_icon_256.png")),
                    new Image(getClass().getResourceAsStream("/images/app_icon_512.png")),
                    new Image(getClass().getResourceAsStream("/images/app_icon.png"))
            );

            primaryStage.setTitle("Loop");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            Platform.runLater(() -> {
                AnimationUtil.attachBackgroundAmbient(root);
                AnimationUtil.enableButtonEffects(root);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Loop — Startup Error");
            alert.setHeaderText("Could not open the application");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Loop");
        launch(args);
    }
}
