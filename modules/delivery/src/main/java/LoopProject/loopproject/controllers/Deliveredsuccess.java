package LoopProject.loopproject.controllers;

import javafx.fxml.FXML;
import main.App;
import main.DriverSession;

import java.io.IOException;

public class Deliveredsuccess {

    @FXML
    public void initialize() {
    }

    @FXML
    private void backToRoutes() throws IOException {
        App.setRoot("driveractiveroutes");
    }

    @FXML
    private void signOut() throws IOException {
        if (!DriverSession.returnToLogin()) {
            App.setRoot("deliveryaccept");
        }
    }
}
