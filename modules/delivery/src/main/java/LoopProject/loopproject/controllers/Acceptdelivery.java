package LoopProject.loopproject.controllers;

import DAO.DeliveryDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import main.App;
import main.DriverSession;
import model.Delivery;

import java.io.IOException;

public class Acceptdelivery {

    @FXML private VBox ordersContainer;
    @FXML private Label statusLabel;

    @FXML
    private void goBack(ActionEvent event) {
        if (!DriverSession.returnToLogin()) {
            try {
                App.setRoot("delivery");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    
    @FXML
    public void initialize() {
        refreshAvailableOrders();
    }

    private void refreshAvailableOrders() {
        ordersContainer.getChildren().clear();
        if (DeliveryDAO.getUnassignedDeliveries().isEmpty()) {
            ordersContainer.getChildren().add(new Label("No deliveries are waiting to be accepted."));
            return;
        }

        for (Delivery delivery : DeliveryDAO.getUnassignedDeliveries()) {
            VBox card = new VBox(7);
            card.getStyleClass().add("order-card");
            Label id = new Label(delivery.getDeliveryId()
                    + "  |  Order #" + delivery.getOrderId());
            id.getStyleClass().add("order-id-label");
            Label name = new Label(delivery.getCustomer());
            name.getStyleClass().add("order-name-label");
            Button accept = new Button("ACCEPT");
            accept.getStyleClass().add("accept-button");
            accept.setOnAction(event -> acceptOrder(delivery));
            card.getChildren().addAll(id, name, accept);
            ordersContainer.getChildren().add(card);
        }
    }

    private void acceptOrder(Delivery delivery) {
        if (!DeliveryDAO.assignDriver(delivery.getOrderId(), DriverSession.getDriverName())) {
            statusLabel.setText("Could not accept order #" + delivery.getOrderId() + ".");
            return;
        }
        try {
            App.setRoot("driveractiveroutes");
        } catch (IOException exception) {
            exception.printStackTrace();
            statusLabel.setText("Delivery accepted, but the active route page could not open.");
        }
    }
    
    
    
}
