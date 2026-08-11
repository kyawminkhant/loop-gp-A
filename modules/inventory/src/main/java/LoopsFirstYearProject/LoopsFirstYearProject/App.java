package LoopsFirstYearProject.LoopsFirstYearProject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import services.InventoryDeliveryService;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage; 
    
    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
        primaryStage = stage;
        
        try {
            InventoryDeliveryService.startAutomaticUpdates();
            LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection.verifySharedSchema();
            System.out.println("Inventory connected to shared database: "
                    + LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection.getDatabasePath());
        } catch (Exception exception) {
            throw new IOException("Inventory cannot open the shared LOOP database.", exception);
        }
         
        String startView = System.getProperty("loop.start", "dashboard");
        Parent root = loadFXML(startView);
        scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 840, 700);
        } else {
            scene.setRoot(root);
        }
        scene.getStylesheets().clear();
         
        stage.setScene(scene);
        stage.setTitle("Inventory Management System");
         
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            System.err.println("Could not load application logo icon. Make sure /images/logo.png exists.");
        }
         
        stage.show();
    }

    @SuppressWarnings("exports")
	public static Stage getStage() {
        return primaryStage;
    }

    @SuppressWarnings("exports")
	public static Scene getScene() {
        return scene;
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    @SuppressWarnings("exports")
	public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxmlFiles/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
