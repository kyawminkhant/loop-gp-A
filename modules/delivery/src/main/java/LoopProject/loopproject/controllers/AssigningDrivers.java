package LoopProject.loopproject.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.App;


public class AssigningDrivers {

    @FXML
    private ImageView ss;

    @FXML
    public void initialize() {
    }
    
    @FXML
    private void goBack() {
        try {
            App.setRoot("delivery");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void Assign(ActionEvent event) throws IOException  {

        //this button find which button is clicked
        Button clickedButton = (Button) event.getSource();

        
        VBox box = (VBox) clickedButton.getParent();

        String orderId = "";
        String driverName = "";
        int labelCount = 0;

        for (int i = 0; i < box.getChildren().size(); i++) {
            if (box.getChildren().get(i) instanceof Label) {
                Label label = (Label) box.getChildren().get(i);

                if (labelCount == 0) {
                    orderId = label.getText();
                } else if (labelCount == 1) {
                    driverName = label.getText();
                }
                labelCount++;
            }
        }

        
        
        System.out.println("Order " + orderId + " assigned to " + driverName);

        clickedButton.setText("Assigned");
        clickedButton.setDisable(true);
        
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxmlfiles/activeDeliveries.fxml"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(root));
        stage.show();
        
        goToActiveDeliveries();
        
    }
    
    private void goToActiveDeliveries() {
        try {
            App.setRoot("activedeliveries");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}