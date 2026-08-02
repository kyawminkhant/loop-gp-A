package LoopProject.loopproject.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.App;
 
public class DeliverySummary {
	   
    @FXML
    private Label activeDeliveriesNumber;
 
    @FXML
    private Label idleDriversNumber;
 
    @FXML
    private Label failedAttemptsNumber;
 
    @FXML
    private Label warehouseBacklogNumber;
 
    @FXML
    public void initialize() {
        
    }
 
    public void updateSummary(int activeDeliveries, int idleDrivers, int failedAttempts, int warehouseBacklog) {
        activeDeliveriesNumber.setText(String.valueOf(activeDeliveries));
        idleDriversNumber.setText(String.valueOf(idleDrivers));
        failedAttemptsNumber.setText(String.valueOf(failedAttempts));
        warehouseBacklogNumber.setText(String.valueOf(warehouseBacklog));
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("delivery");
    }
}
