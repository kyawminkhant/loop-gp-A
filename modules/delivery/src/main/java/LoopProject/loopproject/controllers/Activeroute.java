package LoopProject.loopproject.controllers;
 
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import main.App;
 
public class Activeroute {
 
    @FXML
    private ImageView ss;
 
    @FXML
    public void initialize() {
        
    }
 
    
    @FXML
    private void handleArrived() {
        System.out.println("Marked as arrived");
        goToDeliveredSuccess();
    }

	private void goToDeliveredSuccess() {
		// TODO Auto-generated method stub
		try {
		App.setRoot("deliveredsuccess");
    } catch (IOException e) {
        e.printStackTrace();
    }
		
	}
}
 
