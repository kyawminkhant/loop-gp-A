package LoopProject.loopproject.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import main.App;

public class Acceptdelivery {
    @FXML private ImageView logoImage;

    @FXML private VBox card204;
    @FXML private VBox card205;
    @FXML private VBox card203;
    @FXML private VBox card207;
    @FXML private VBox card208;

    @FXML
    public void initialize() {
       
    }

    @FXML
    private void handleAccept204() {
        acceptOrder("ORD-204", card204);
    }

    @FXML
    private void handleAccept205() {
        acceptOrder("ORD-205", card205);
    }

    @FXML
    private void handleAccept203() {
        acceptOrder("ORD-203", card203);
    }

    @FXML
    private void handleAccept207() {
        acceptOrder("ORD-207", card207);
    }

    @FXML
    private void handleAccept208() {
        acceptOrder("ORD-208", card208);
    }


    private void acceptOrder(String orderId, VBox card) {

        card.setVisible(false);
        card.setManaged(false); 

        goToActiveRoute(); 
    }

    private void goToActiveRoute() {
        try {
            App.setRoot("driveractiveroutes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
