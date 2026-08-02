package LoopsFirstYearProject.LoopsFirstYearProject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage; 
    
    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
        primaryStage = stage;
        
        try {
            LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection.testConnection1();
            LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection.testConnection2();
        } catch (Exception e) {
            e.printStackTrace();
        }
         
        scene = new Scene(loadFXML("signup"), 840, 700);
         
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