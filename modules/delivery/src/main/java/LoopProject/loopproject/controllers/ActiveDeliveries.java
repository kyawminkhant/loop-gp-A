package LoopProject.loopproject.controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import main.App;

public class ActiveDeliveries {

    @FXML
    private ImageView ss;
    
 
    @FXML
    public void initialize() {
        
    }
    
    @FXML
    private void goBack(ActionEvent event) {
        try {
            App.setRoot("assigningDrivers");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    @FXML
    private void onRoute(ActionEvent event) {
 
        
        Button clickedButton = (Button) event.getSource();
 
       
        VBox card = (VBox) clickedButton.getParent();
 
        String driverId = "";
        String route = "";
        int labelCount = 0;
 
        for (int i = 0; i < card.getChildren().size(); i++) {
            if (card.getChildren().get(i) instanceof Label) {
                Label label = (Label) card.getChildren().get(i);
 
                if (labelCount == 0) {
                    driverId = label.getText();
                } else if (labelCount == 1) {
                    route = label.getText();
                }
                labelCount++;
            }
        }
        
        
 
        System.out.println(driverId + " (" + route + ") marked as delivered");
 
        // when on route button clicked delivered button comes next 
        if (clickedButton.getText().equals("On route")) {
            clickedButton.setText("Delivered");
        } else {
            clickedButton.setText("On route");
        }
    }
}
