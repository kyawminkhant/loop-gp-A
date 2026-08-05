package LoopProject.loopproject.controllers;

import DAO.DeliveryDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import main.App;
import model.Delivery;

import java.io.IOException;

public class ActiveDeliveries {

    @FXML private VBox activeDeliveryList;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        refreshActiveDeliveries();
    }

    private void refreshActiveDeliveries() {
        activeDeliveryList.getChildren().clear();
        if (DeliveryDAO.getActiveDeliveries().isEmpty()) {
            activeDeliveryList.getChildren().add(new Label("No active deliveries."));
            return;
        }

        for (Delivery delivery : DeliveryDAO.getActiveDeliveries()) {
            VBox card = new VBox(7);
            card.getStyleClass().add("deliverybox");
            Label id = new Label(delivery.getDeliveryId()
                    + "  |  Order #" + delivery.getOrderId());
            id.getStyleClass().add("delivery-id");
            Label route = new Label(delivery.getDriver() + "  |  "
                    + delivery.getCustomer() + "  |  " + delivery.getStatus());
            route.getStyleClass().add("delivery-route");
            Button delivered = new Button("Mark Delivered");
            delivered.getStyleClass().add("status-button");
            delivered.setOnAction(event -> {
                boolean updated = DeliveryDAO.markDelivered(delivery.getOrderId());
                statusLabel.setText(updated
                        ? "Order #" + delivery.getOrderId() + " marked Delivered in both components."
                        : "Could not update order #" + delivery.getOrderId() + ".");
                refreshActiveDeliveries();
            });
            card.getChildren().addAll(id, route, delivered);
            activeDeliveryList.getChildren().add(card);
        }
    }

    @FXML
    private void goBack() {
        try {
            App.setRoot("assigningDrivers");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
